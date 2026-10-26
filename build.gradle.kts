import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    `kotlin-dsl`
    `maven-publish`
    id("com.gradle.plugin-publish") version "2.0.0"
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
    website = "https://github.com/matsudamper/gradle-transitive-version-lint-plugin"
    vcsUrl = "https://github.com/matsudamper/gradle-transitive-version-lint-plugin.git"
    plugins {
        register("transitiveVersionLint") {
            id = "net.matsudamper.transitiveversionlint"
            implementationClass = "net.matsudamper.transitiveversionlint.CheckTransitiveDependenciesPlugin"
            displayName = "Transitive Version Lint"
            description = "Verifies that specified library versions are not unintentionally changed by transitive dependencies."
            tags = listOf("dependencies", "lint", "android", "transitive")
        }
    }
}
