plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.edisonsolar.attendance"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.edisonsolar.attendance"
        minSdk = 24
        targetSdk = 35
        versionCode = 4
        versionName = "4.0"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }
}
