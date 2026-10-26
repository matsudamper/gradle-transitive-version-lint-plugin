plugins {
    id("com.android.application")
    id("net.matsudamper.transitiveversionlint")
}

android {
    namespace = "net.matsudamper.transitiveversionlint.sample.configurationnamepatterns"
    compileSdk = 36
    defaultConfig {
        minSdk = 24
    }
}

// configurationNamePatterns で指定した debugRuntimeClasspath の問題を検知することを verifySample で確認する
dependencies {
    // OkHttp 4.12.0 は okio 3.6.0 を取り込むため、直接宣言した 3.0.0 から推移的依存でバージョンが上がる。
    debugImplementation("com.squareup.okio:okio:3.18.2")
    debugImplementation("com.squareup.okhttp3:okhttp:4.12.0")
}

checkTransitiveDependencies {
    // エラー内容を verifySample で確認するため、ここではビルドを失敗させない
    ignoreFailures = true
    configurationNamePatterns.set(listOf(".*[dD]ebugRuntimeClasspath"))
    fixedLibraryString.addAll(
        "com.squareup.okio:okio:3.18.2",
    )
}

tasks.register("verifySample") {
    group = "verification"
    dependsOn("verifyTransitiveDependencies")

    val report = layout.buildDirectory.file("reports/transitive-dependencies/report.txt")
    val expectedErrors = listOf(
        "Transitive Dependencies Detected: com.squareup.okio:okio:3.0.0\nResolved Version: 3.6.0 (debugRuntimeClasspath)",
    )
    doLast {
        val reportText = report.get().asFile.readText()
        val missingErrors = expectedErrors.filterNot { it in reportText }
        check(missingErrors.isEmpty()) {
            buildString {
                appendLine("検知できていないエラーがあります")
                missingErrors.forEach { appendLine(" - $it") }
            }
        }
    }
}
