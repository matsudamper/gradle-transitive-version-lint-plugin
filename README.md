# gradle-transitive-version-lint-plugin

A Gradle plugin that verifies the versions of specified libraries are not unintentionally changed by transitive dependencies.
It works with JVM (`java`, `org.jetbrains.kotlin.jvm`, etc.) and Android modules.

## Usage

Apply the plugin in your module's `build.gradle.kts` and specify the libraries whose versions you want to pin.

```kotlin
plugins {
    id("net.matsudamper.transitiveversionlint") version "<version>"
}

checkTransitiveDependencies {
    // Specify in group:name:version format
    fixedLibraryString.addAll(
        "com.squareup.okio:okio:3.6.0",
    )
    // Version Catalog entries are also supported
    fixedLibrary.add(libs.okhttp)

    // The following are optional. The values shown are the defaults

    // Regular expressions for the configuration names to verify. A configuration is verified when its whole name matches
    // runtimeClasspath is for JVM, .*[rR]eleaseRuntimeClasspath is for Android. The buildscript classpath is always verified
    configurationNamePatterns.set(
        listOf("runtimeClasspath", ".*[rR]eleaseRuntimeClasspath"),
    )
    // When true, the task does not fail on errors; it only writes the report and logs warnings
    ignoreFailures = false
    // Output location of the report
    reportPath = layout.buildDirectory.file("reports/transitive-dependencies/report.txt").get().asFile
}
```

Run the verification.

```shell
./gradlew :verifyTransitiveDependencies
```

## What it detects

The `verifyTransitiveDependencies` task checks `runtimeClasspath` (JVM), `*ReleaseRuntimeClasspath` (Android), and the buildscript `classpath`,
and reports the following for each specified library.

| Error | Description |
| --- | --- |
| `Transitive Dependencies Detected` | The finally resolved version differs from the specified version |
| `Multiple Versions Detected` | Different versions are declared directly by modules in the project |
| `Library Not Used` | The library is not in the dependency graph, or is not declared directly by any module |

## Samples

[`sample`](sample)
