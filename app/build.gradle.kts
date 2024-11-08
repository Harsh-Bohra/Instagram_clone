plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.google.gms.google.services)
}

android {
    namespace = "my.insta.androrealm"
    compileSdk = 34

    defaultConfig {
        applicationId = "my.insta.androrealm"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {
    // Firebase Dependencies
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.storage)
    implementation(libs.firebase.core)
    implementation(libs.firebase.messaging)

    // Android Libraries (AndroidX only)
    implementation(libs.appcompat)  // AndroidX version of the Support Library
    implementation(libs.material.v100)
    implementation(libs.constraintlayout)

    // CircleImageView and BottomNavigationView
    implementation(libs.circleimageview)

    // Image Libraries
    implementation(libs.universal.image.loader)
    implementation(libs.glide.v4110)
    implementation(libs.firebase.auth.v1940)
    implementation(libs.firebase.database)
    annotationProcessor(libs.compiler)

    // Retrofit for Networking
    implementation(libs.retrofit)
    implementation(libs.converter.gson)

    // UI Components
    implementation(libs.cardview)
    implementation(libs.library) // Assuming this is required, but needs clarification

    // Testing Libraries
    testImplementation(libs.junit.v412)
    androidTestImplementation(libs.junit.v112)
    androidTestImplementation(libs.espresso.core.v330)
    androidTestImplementation(libs.ext.junit)
}
