plugins {
    id("com.android.application")
    id("net.matsudamper.transitiveversionlint")
}

android {
    namespace = "net.matsudamper.transitiveversionlint.sample.error"
    compileSdk = 36
    defaultConfig {
        minSdk = 24
    }
}

// verifyTransitiveDependencies が以下をそれぞれ検知することを verifySample で確認する
dependencies {
    // OkHttp 4.12.0 は okio 3.6.0 を取り込むため、直接宣言した 3.0.0 から推移的依存でバージョンが上がる。
    implementation("com.squareup.okio:okio:3.0.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")

    // アプリの依存では 2.1.20 のまま解決されるが、AGP が buildscript の classpath に kotlin-stdlib を持ち込み、
    // Gradle が同梱している Kotlin のバージョンに上げる。
    implementation("org.jetbrains.kotlin:kotlin-stdlib:2.1.20")
}

checkTransitiveDependencies {
    // エラー内容を verifySample で確認するため、ここではビルドを失敗させない
    ignoreFailures = true
    fixedLibraryString.addAll(
        "com.squareup.okio:okio:3.0.0",
        "org.jetbrains.kotlin:kotlin-stdlib:2.1.20",
    )
}

tasks.register("verifySample") {
    group = "verification"
    dependsOn("verifyTransitiveDependencies")

    val report = layout.buildDirectory.file("reports/transitive-dependencies/report.txt")
    val expectedErrors = listOf(
        "Transitive Dependencies Detected: com.squareup.okio:okio:3.0.0",
        "Transitive Dependencies Detected: org.jetbrains.kotlin:kotlin-stdlib:2.1.20",
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
