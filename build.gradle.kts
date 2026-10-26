import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    `kotlin-dsl`
    `maven-publish`
}

group = "net.matsudamper.transitiveversionlint"
version = providers.gradleProperty("publishVersion").filter { it.isNotBlank() }.getOrElse("latest-SNAPSHOT")

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
    withSourcesJar()
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_17
    }
}

gradlePlugin {
    plugins {
        register("transitiveVersionLint") {
            id = "net.matsudamper.transitiveversionlint"
            implementationClass = "net.matsudamper.transitiveversionlint.CheckTransitiveDependenciesPlugin"
        }
    }
}
