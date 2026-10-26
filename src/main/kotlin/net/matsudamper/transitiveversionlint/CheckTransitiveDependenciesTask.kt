package net.matsudamper.transitiveversionlint

import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.work.DisableCachingByDefault

@DisableCachingByDefault(because = "依存関係の検証のみを行うタスクで、キャッシュする価値がないため")
abstract class CheckTransitiveDependenciesTask : DefaultTask() {
    @get:Input
    abstract val fixedLibraries: ListProperty<String>

    @get:OutputFile
    abstract val reportFile: RegularFileProperty

    @get:Input
    abstract val configurationSnapshots: ListProperty<ConfigurationSnapshot>

    @get:Input
    abstract val buildTreePath: Property<String>

    @get:Input
    abstract val ignoreFailures: Property<Boolean>

    @TaskAction
    fun check() {
        val taskName = "${buildTreePath.get()}:${this.name}"
        val file = reportFile.get().asFile

        file.parentFile.mkdirs()

        val errors = getErrors(
            configurations = configurationSnapshots.get(),
            fixedLibraries = fixedLibraries.get(),
        )

        if (errors.isNotEmpty()) {
            val throwable = createError(
                errors = errors,
                taskName = taskName,
            )

            file.writeText(
                throwable.message.orEmpty(),
            )

            if (ignoreFailures.get()) {
                logger.warn(throwable.message)
            } else {
                throw throwable
            }
        } else {
            val report = buildString {
                appendLine("Complete $taskName")
                appendLine(
                    fixedLibraries.get()
                        .joinToString("\n"),
                )
            }
            file.writeText(report)
            println(report)
        }
    }

    private fun createError(
        errors: List<Errors>,
        taskName: String,
    ): Throwable {
        val errorString = errors.joinToString("\n\n") { error ->
            when (error) {
                is Errors.LibraryNotUsed -> {
                    "Library Not Used: ${error.fixedLibrary}"
                }

                is Errors.MultipleVersions -> {
                    buildString {
                        appendLine("Multiple Versions Detected: ${error.requestLibrary}")
                        for (moduleVersion in error.moduleVersions) {
                            appendLine(" - $moduleVersion")
                        }
                    }.trim()
                }

                is Errors.TransitiveDependencies -> {
                    buildString {
                        appendLine("Transitive Dependencies Detected: ${error.requestLibrary}")
                        for (resolvedVersion in error.resolvedVersions) {
                            appendLine("Resolved Version: ${resolvedVersion.version} (${resolvedVersion.configuration})")
                        }
                    }.trim()
                }
            }
        }
        return AssertionError(
            buildString {
                appendLine("Failed $taskName")
                append(errorString)
            },
        )
    }

    private fun getErrors(
        configurations: List<ConfigurationSnapshot>,
        fixedLibraries: List<String>,
    ): List<Errors> {
        val errors = mutableListOf<Errors>()
        fixedLibraries
            .forEach { targetLibrary ->
                val fixedGroup = targetLibrary.split(":")[0]
                val fixedName = targetLibrary.split(":")[1]
                val fixedVersion = targetLibrary.split(":")[2]

                val resolvedVersions = getTransitiveVersions(
                    configurations = configurations,
                    fixedGroup = fixedGroup,
                    fixedName = fixedName,
                )
                if (resolvedVersions.isEmpty()) {
                    errors.add(
                        Errors.LibraryNotUsed(
                            fixedLibrary = targetLibrary,
                        ),
                    )
                    return@forEach
                }

                val requestVersions = getRequestVersion(
                    configurations = configurations,
                    fixedGroup = fixedGroup,
                    fixedModule = fixedName,
                )

                run {
                    // 依存グラフには居るが、このリポジトリのどのモジュールからも直接宣言されていない。
                    // バージョンを固定できないので、fixedLibraryから外すか明示的に宣言する必要がある
                    if (requestVersions.isEmpty()) {
                        errors.add(
                            Errors.LibraryNotUsed(
                                fixedLibrary = targetLibrary,
                            ),
                        )

                        return@forEach
                    }

                    // BOM等でバージョンが指定されていないリクエストは比較対象外にする
                    val versionedRequests = requestVersions.filter { it.version.isNotBlank() }
                    val requestedVersions = versionedRequests.map { it.version }.distinct()
                    if (requestedVersions.size > 1) {
                        errors.add(
                            Errors.MultipleVersions(
                                requestLibrary = targetLibrary,
                                moduleVersions = versionedRequests.map {
                                    "${it.fromModule}: ${it.version}"
                                },
                            ),
                        )
                    }
                }

                val mismatchedVersions = resolvedVersions.filter { it.version != fixedVersion }
                if (mismatchedVersions.isNotEmpty()) {
                    errors.add(
                        Errors.TransitiveDependencies(
                            requestLibrary = targetLibrary,
                            resolvedVersions = mismatchedVersions,
                        ),
                    )
                }
            }

        return errors.distinct()
    }

    /**
     * 実行されたプロジェクトが直接記述したライブラリのバージョンを取得
     */
    private fun getRequestVersion(
        configurations: List<ConfigurationSnapshot>,
        fixedGroup: String,
        fixedModule: String,
    ): List<RequestVersionInfo> {
        val targetDeps = getResolvedDependencyResults(
            configurations = configurations,
            fixedGroup = fixedGroup,
            fixedName = fixedModule,
        )

        val directlyDefinedVersions = targetDeps
            .mapNotNull { resolvedDep ->
                val fromModule = when (val fromId = resolvedDep.fromId) {
                    is DependsFrom.Module -> fromId
                    // 外部ライブラリからのリクエストは対象外
                    is DependsFrom.Library -> return@mapNotNull null
                }

                RequestVersionInfo(
                    version = resolvedDep.requestedVersion,
                    fromModule = fromModule.name,
                )
            }

        return directlyDefinedVersions.distinct()
    }

    /**
     * configuration ごとに最終的に解決されたバージョンを取得
     */
    private fun getTransitiveVersions(
        configurations: List<ConfigurationSnapshot>,
        fixedGroup: String,
        fixedName: String,
    ): List<ResolvedVersion> {
        return configurations.mapNotNull { configuration ->
            val targetDep = getResolvedDependencyResults(
                configurations = listOf(configuration),
                fixedGroup = fixedGroup,
                fixedName = fixedName,
            ).firstOrNull() ?: return@mapNotNull null

            ResolvedVersion(
                configuration = configuration.name,
                version = targetDep.selectedId.version,
            )
        }.distinct()
    }

    private fun getResolvedDependencyResults(
        configurations: List<ConfigurationSnapshot>,
        fixedGroup: String,
        fixedName: String,
    ): List<ResolvedDependencySnapshot> {
        return configurations.flatMap { configuration ->
            configuration.dependencies
                .filter { dep ->
                    val id = dep.selectedId
                    id.group == fixedGroup &&
                        id.module == fixedName
                }.toList()
        }
    }

    data class RequestVersionInfo(
        val version: String,
        val fromModule: String,
    )

    data class ResolvedVersion(
        val configuration: String,
        val version: String,
    )

    sealed interface Errors {
        data class TransitiveDependencies(
            val requestLibrary: String,
            val resolvedVersions: List<ResolvedVersion>,
        ) : Errors

        data class MultipleVersions(
            val requestLibrary: String,
            val moduleVersions: List<String>,
        ) : Errors

        data class LibraryNotUsed(
            val fixedLibrary: String,
        ) : Errors
    }
}
