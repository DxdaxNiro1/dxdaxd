plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.dreamcore.game"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.dreamcore.game"
        minSdk = 19
        targetSdk = 34
        versionCode = 35
        versionName = "3.4"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }
    buildTypes {
        release { isMinifyEnabled = false }
    }
}
