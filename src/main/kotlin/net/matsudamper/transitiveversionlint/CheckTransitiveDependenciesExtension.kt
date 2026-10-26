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

    @Input
    val reportPath: Property<File> =
        objects.property(File::class.java)
}
