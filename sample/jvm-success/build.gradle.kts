plugins {
    java
    id("net.matsudamper.transitiveversionlint")
}

dependencies {
    // OkHttp 4.12.0 が取り込む okio は 3.6.0 なので、直接宣言した 3.6.0 のまま解決される
    implementation("com.squareup.okio:okio:3.6.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.1")
}

checkTransitiveDependencies {
    fixedLibraryString.addAll(
        "com.squareup.okio:okio:3.6.0",
        "com.squareup.okhttp3:okhttp:4.12.1",
    )
}

// エラーがあれば verifyTransitiveDependencies が失敗する
tasks.register("verifySample") {
    group = "verification"
    dependsOn("verifyTransitiveDependencies")
}
