plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.edisonsolar.businesspro"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.edisonsolar.businesspro"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {

    implementation("androidx.appcompat:appcompat:1.7.1")

    implementation("androidx.core:core:1.17.0")

    implementation("com.google.android.material:material:1.13.0")
}
