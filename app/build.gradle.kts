plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}
android {
    namespace = "com.model01.molecule"
    compileSdk = 35
    defaultConfig {
        applicationId = "com.model01.molecule"
        minSdk = 23
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
    }
}
