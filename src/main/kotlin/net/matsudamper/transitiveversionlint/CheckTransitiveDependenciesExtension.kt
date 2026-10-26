package net.matsudamper.transitiveversionlint

import org.gradle.api.artifacts.MinimalExternalModuleDependency
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import java.io.File
import javax.inject.Inject

open class CheckTransitiveDependenciesExtension @Inject constructor(
    objects: ObjectFactory,
) {
    /**
     * group:name:version の形式
     */
    @Input
    val fixedLibraryString: ListProperty<String> =
        objects.listProperty(String::class.java)
            .convention(listOf())

    @Input
    val fixedLibrary: ListProperty<MinimalExternalModuleDependency> =
        objects.listProperty(MinimalExternalModuleDependency::class.java)
            .convention(listOf())

    /**
     * 検証対象の configuration 名の正規表現。名前全体に一致したものが対象になる。
     * デフォルトは JVM の runtimeClasspath と Android の *ReleaseRuntimeClasspath
     */
    @Input
    val configurationNamePatterns: ListProperty<String> =
        objects.listProperty(String::class.java)
            .convention(listOf("runtimeClasspath", ".*[rR]eleaseRuntimeClasspath"))

    @Input
    val reportPath: Property<File> =
        objects.property(File::class.java)

    /**
     * true の場合、エラーがあってもタスクを失敗させずにレポートの出力と警告のみ行う
     */
    @Input
    val ignoreFailures: Property<Boolean> =
        objects.property(Boolean::class.java)
            .convention(false)
}
