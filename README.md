# gradle-transitive-version-lint-plugin

指定したライブラリのバージョンが、推移的依存によって意図せず変わっていないかを検証する Gradle プラグインです。
JVM(`java`、`org.jetbrains.kotlin.jvm` など)と Android のモジュールで使えます。

## 使い方

モジュールの `build.gradle.kts` にプラグインを適用し、バージョンを固定したいライブラリを指定します。

```kotlin
plugins {
    id("net.matsudamper.transitiveversionlint") version "<version>"
}

checkTransitiveDependencies {
    // group:name:version の形式で指定
    fixedLibraryString.addAll(
        "com.squareup.okio:okio:3.6.0",
    )
    // Version Catalog のエントリも指定できる
    fixedLibrary.add(libs.okhttp)

    // 以下は任意。値はデフォルト

    // 検証対象の configuration 名の正規表現。名前全体が一致したものが対象になる
    // runtimeClasspath は JVM、.*[rR]eleaseRuntimeClasspath は Android 向け。buildscript の classpath は常に対象
    configurationNamePatterns.set(
        listOf("runtimeClasspath", ".*[rR]eleaseRuntimeClasspath"),
    )
    // true にすると、エラーがあってもタスクを失敗させず、レポートの出力と警告のみ行う
    ignoreFailures = false
    // レポートの出力先
    reportPath = layout.buildDirectory.file("reports/transitive-dependencies/report.txt").get().asFile
}
```

検証を実行します。

```shell
./gradlew :verifyTransitiveDependencies
```

## 検知する内容

`verifyTransitiveDependencies` タスクは `runtimeClasspath`(JVM)、`*ReleaseRuntimeClasspath`(Android)と buildscript の `classpath` を対象に、
指定したライブラリごとに以下をチェックします。

| エラー | 内容 |
| --- | --- |
| `Transitive Dependencies Detected` | 最終的に解決されたバージョンが指定バージョンと異なる |
| `Multiple Versions Detected` | プロジェクト内のモジュールから異なるバージョンが直接宣言されている |
| `Library Not Used` | 依存グラフに存在しない、またはどのモジュールからも直接宣言されていない |

## サンプル

[`sample`](sample) 
