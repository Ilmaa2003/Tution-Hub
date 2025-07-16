plugins {
    alias(libs.plugins.android.application)
    id("com.google.gms.google-services") // ✅ Required for Firebase services
}

android {
    namespace = "com.nibm.tutionmanagement"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.nibm.tutionmanagement"
        minSdk = 27
        targetSdk = 35
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    // Android UI and AppCompat
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)

    // CardView for dashboard UI
    implementation("androidx.cardview:cardview:1.0.0")

    // Charting library
    implementation("com.github.PhilJay:MPAndroidChart:3.1.0")

    // Firebase BoM to manage versions
    implementation(platform("com.google.firebase:firebase-bom:33.15.0"))

    // Firebase Products
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-database")
    implementation("com.google.firebase:firebase-auth")

    // Image loading with Glide
    implementation("com.github.bumptech.glide:glide:4.16.0")
    annotationProcessor("com.github.bumptech.glide:compiler:4.16.0")


    // ZXing QR Code scanning and generation libraries
    implementation("com.journeyapps:zxing-android-embedded:4.3.0")  // Main ZXing Android lib
    implementation("com.google.zxing:core:3.4.1")                 // ZXing core


    // Unit testing
    testImplementation(libs.junit)

    // Android testing
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}

// ADD THIS at the end
apply(plugin = "com.google.gms.google-services")
