package net.matsudamper.transitiveversionlint

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.component.ModuleComponentIdentifier
import org.gradle.api.artifacts.component.ModuleComponentSelector
import org.gradle.api.artifacts.component.ProjectComponentIdentifier
import org.gradle.api.artifacts.component.RootComponentIdentifier
import org.gradle.api.artifacts.result.ResolvedDependencyResult
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.register
import java.io.File

class CheckTransitiveDependenciesPlugin : Plugin<Project> {
    override fun apply(target: Project) {

        with(target) {
            val thisExtension = extensions
                .create<CheckTransitiveDependenciesExtension>("checkTransitiveDependencies")

            tasks.register<CheckTransitiveDependenciesTask>(TASK_NAME) {
                group = "verification"
                description =
                    "指定されたライブラリが推移的依存でバージョンが変わっていないかを確認する"

                @Suppress("UnstableApiUsage")
                buildTreePath.set(project.buildTreePath)

                val reportFileValue = thisExtension.reportPath.orNull ?: File(
                    project.layout.buildDirectory.asFile.get(),
                    "reports/transitive-dependencies/report.txt",
                )
                reportFile.set(reportFileValue)

                fixedLibraries.set(
                    thisExtension.fixedLibraryString.zip(thisExtension.fixedLibrary) { strings, deps ->
                        strings + deps.map { "${it.group}:${it.name}:${it.version}" }
                    },
                )

                configurationSnapshots.set(
                    provider {
                        check(pluginManager.hasPlugin("com.android.application")) {
                            "${project.path} is not an Android application project"
                        }

                        val configurations = run {
                            val releaseRuntimeConfigurations = target.configurations.filter { config ->
                                config.name.endsWith("releaseRuntimeClasspath") || config.name.endsWith("ReleaseRuntimeClasspath")
                            }
                            val pluginConfigurations = target.buildscript.configurations
                                .plus(target.rootProject.buildscript.configurations)
                                .filter { config ->
                                    config.name == "classpath"
                                }
                            releaseRuntimeConfigurations
                                .plus(pluginConfigurations)
                        }

                        createConfigurationSnapshots(configurations)
                    },
                )
            }
        }
    }

    private fun createConfigurationSnapshots(
        configurations: List<Configuration>,
    ): List<ConfigurationSnapshot> {
        return configurations.map { configuration ->
            val dependencies = configuration.incoming.resolutionResult.allDependencies
                .filterIsInstance<ResolvedDependencyResult>()
                .filter { dep ->
                    // constraints や BOM、Gradle が付ける strictly 制約は依存の宣言ではない
                    !dep.isConstraint
                }
                .filter { dep ->
                    // 外部ライブラリ
                    dep.selected.id is ModuleComponentIdentifier
                }
                .map { dep ->
                    val selectedId = dep.selected.id as ModuleComponentIdentifier
                    val requestedVersion = when (val requested = dep.requested) {
                        is ModuleComponentSelector -> requested.version
                        else -> ""
                    }

                    ResolvedDependencySnapshot(
                        selectedId = ModuleId(
                            group = selectedId.group,
                            module = selectedId.module,
                            version = selectedId.version,
                        ),
                        fromId = when (val id = dep.from.id) {
                            is ModuleComponentIdentifier,
                                -> {
                                DependsFrom.Library(
                                    id = ModuleId(
                                        group = id.group,
                                        module = id.module,
                                        version = id.version,
                                    ),
                                )
                            }

                            is ProjectComponentIdentifier,
                            is RootComponentIdentifier,
                                -> {
                                DependsFrom.Module(
                                    name = id.displayName,
                                )
                            }

                            else -> TODO("${id::class} not implemented")
                        },
                        requestedVersion = requestedVersion,
                    )
                }

            ConfigurationSnapshot(
                name = configuration.name,
                dependencies = dependencies,
            )
        }
    }

    companion object {
        private const val TASK_NAME = "verifyTransitiveDependencies"
    }
}
