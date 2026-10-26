pluginManagement {
    // ルートのプラグインをビルドして参照する
    includeBuild("..")
    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "sample"

include(":app-success")
include(":app-transitive-error")
include(":app-configuration-name-patterns")
include(":jvm-success")
include(":jvm-configuration-name-patterns")
