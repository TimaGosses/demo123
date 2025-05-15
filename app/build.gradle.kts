plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    kotlin("plugin.serialization") version "2.1.0"

}



android {
    namespace = "com.example.demo123"
    compileSdk = 35

    buildFeatures {
        viewBinding = true
    }

    defaultConfig {
        applicationId = "com.example.demo123"
        minSdk = 24
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
    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {


    implementation("io.coil-kt.coil3:coil-compose:3.1.0") //Photo Picker
    implementation("androidx.activity:activity-compose:1.10.1")
    implementation ("com.github.bumptech.glide:glide:5.0.0-rc01")
    implementation("io.github.jan-tennert.supabase:postgrest-kt:3.1.3")
    implementation("io.github.jan-tennert.supabase:storage-kt:3.1.4")
    implementation ("androidx.security:security-crypto:1.0.0")
    implementation("io.github.jan-tennert.supabase:auth-kt:3.1.4")
    implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.1")
    implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")
    implementation("io.ktor:ktor-client-cio:3.1.1")
    implementation ("org.jetbrains.kotlinx:kotlinx-serialization-json")
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}