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
include(":jvm-success")
