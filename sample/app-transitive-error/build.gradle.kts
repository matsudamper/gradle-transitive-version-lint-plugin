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

dependencies {
    // OkHttp 4.12.0 は okio 3.6.0 を取り込むため、直接宣言した 3.0.0 から推移的依存でバージョンが上がる。
    // verifyTransitiveDependencies が失敗することを CI で確認する
    implementation("com.squareup.okio:okio:3.0.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
}

checkTransitiveDependencies {
    fixedLibraryString.addAll(
        "com.squareup.okio:okio:3.0.0",
    )
}
