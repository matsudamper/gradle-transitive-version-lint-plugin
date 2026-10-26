plugins {
    id("com.android.application")
    id("net.matsudamper.transitiveversionlint")
}

android {
    namespace = "net.matsudamper.transitiveversionlint.sample.transitiveupgrade"
    compileSdk = 36
    defaultConfig {
        minSdk = 24
    }
}

// releaseRuntimeClasspath で推移的依存によるバージョン変更を検知することを verifySample で確認する
dependencies {
    // OkHttp 4.12.0 は okio 3.6.0 を取り込むため、直接宣言した 3.0.0 から推移的依存でバージョンが上がる。
    implementation("com.squareup.okio:okio:3.0.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.1")
}

checkTransitiveDependencies {
    // エラー内容を verifySample で確認するため、ここではビルドを失敗させない
    ignoreFailures = true
    fixedLibraryString.addAll(
        "com.squareup.okio:okio:3.0.0",
    )
}

tasks.register("verifySample") {
    group = "verification"
    dependsOn("verifyTransitiveDependencies")

    val report = layout.buildDirectory.file("reports/transitive-dependencies/report.txt")
    val expectedErrors = listOf(
        "Transitive Dependencies Detected: com.squareup.okio:okio:3.0.0\nResolved Version: 3.6.0 (releaseRuntimeClasspath)",
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
