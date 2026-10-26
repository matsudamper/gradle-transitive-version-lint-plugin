plugins {
    id("com.android.application")
    id("net.matsudamper.transitiveversionlint")
}

android {
    namespace = "net.matsudamper.transitiveversionlint.sample.buildscriptclasspathupgrade"
    compileSdk = 36
    defaultConfig {
        minSdk = 24
    }
}

// buildscript の classpath でのバージョン変更を検知することを verifySample で確認する
dependencies {
    // アプリの依存では 2.1.20 のまま解決されるが、AGP が buildscript の classpath に kotlin-stdlib を持ち込み、
    // Gradle が同梱している Kotlin のバージョンに上げる。
    implementation("org.jetbrains.kotlin:kotlin-stdlib:2.1.20")
}

checkTransitiveDependencies {
    // エラー内容を verifySample で確認するため、ここではビルドを失敗させない
    ignoreFailures = true
    fixedLibraryString.addAll(
        "org.jetbrains.kotlin:kotlin-stdlib:2.1.20",
    )
}

tasks.register("verifySample") {
    group = "verification"
    dependsOn("verifyTransitiveDependencies")

    val report = layout.buildDirectory.file("reports/transitive-dependencies/report.txt")
    // 解決後のバージョンは Gradle 同梱の Kotlin に依存するため、検知元の configuration だけを確認する
    val expectedErrors = listOf(
        "Transitive Dependencies Detected: org.jetbrains.kotlin:kotlin-stdlib:2.1.20",
        "(classpath)",
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
