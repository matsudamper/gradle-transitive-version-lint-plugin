plugins {
    java
    id("net.matsudamper.transitiveversionlint")
}

dependencies {
    // OkHttp 4.12.0 が取り込む okio は 3.6.0 なので、直接宣言した 3.6.0 のまま解決される
    implementation("com.squareup.okio:okio:3.6.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")

    // testRuntimeClasspath にだけ含まれる
    testImplementation("junit:junit:4.13.2")
}

checkTransitiveDependencies {
    // デフォルトの runtimeClasspath に加えて testRuntimeClasspath も検証する
    configurationNamePatterns.set(listOf("runtimeClasspath", "testRuntimeClasspath"))
    fixedLibraryString.addAll(
        "com.squareup.okio:okio:3.6.0",
        "com.squareup.okhttp3:okhttp:4.12.0",
        "junit:junit:4.13.2",
    )
}

// エラーがあれば verifyTransitiveDependencies が失敗する
tasks.register("verifySample") {
    group = "verification"
    dependsOn("verifyTransitiveDependencies")
}
