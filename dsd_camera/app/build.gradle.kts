plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.jetbrainsKotlinAndroid)
}

android {
    namespace = "com.sneha.dsdcamera"
    compileSdk = 34
    // fixme possible error
    aaptOptions.noCompress("tflite")


    defaultConfig {
        applicationId = "com.sneha.dsdcamera"
        minSdk = 28
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
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.11"
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)



    implementation("com.google.guava:guava:31.0.1-jre")

    // To use CallbackToFutureAdapter
    implementation("androidx.concurrent:concurrent-futures:1.1.0")

    // Kotlin
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-guava:1.7.3")



    implementation("androidx.navigation:navigation-compose:2.7.7")

    implementation("com.google.mlkit:image-labeling-custom:17.0.2")
    implementation ("org.tensorflow:tensorflow-lite:2.4.0")
    implementation("org.tensorflow:tensorflow-lite-support:0.1.0")



    val composeBom = platform("androidx.compose:compose-bom:2024.04.01")
    implementation(composeBom)
    androidTestImplementation(composeBom)

    // Choose one of the following:
    // Material Design 3
    implementation("androidx.compose.material3:material3")
    // or Material Design 2
    implementation("androidx.compose.material:material")
    // or skip Material Design and build directly on top of foundational components
    implementation("androidx.compose.foundation:foundation")
    // or only import the main APIs for the underlying toolkit systems,
    // such as input and measurement/layout
    implementation("androidx.compose.ui:ui")

    // Android Studio Preview support
    implementation("androidx.compose.ui:ui-tooling-preview")
    debugImplementation("androidx.compose.ui:ui-tooling")

    // UI Tests
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-test-manifest")

    implementation("androidx.activity:activity-compose:1.9.0")



    val cameraxVersion = "1.3.3"
    // The following line is optional, as the core library is included indirectly by camera-camera2
    implementation("androidx.camera:camera-core:$cameraxVersion")
    implementation("androidx.camera:camera-camera2:$cameraxVersion")

    // If you want to additionally use the CameraX Lifecycle library
    implementation("androidx.camera:camera-lifecycle:$cameraxVersion")

    // If you want to additionally use the CameraX VideoCapture library
    implementation("androidx.camera:camera-video:$cameraxVersion")

    // If you want to additionally use the CameraX View class
    implementation("androidx.camera:camera-view:$cameraxVersion")

    // If you want to additionally add CameraX ML Kit Vision Integration
    implementation("androidx.camera:camera-mlkit-vision:1.4.0-beta01")

    // If you want to additionally use the CameraX Extensions library
    implementation("androidx.camera:camera-extensions:$cameraxVersion")

    // Separate version for camera-viewfinder, potentially can use cameraxVersion as well if same
    implementation("androidx.camera:camera-viewfinder:1.3.0-beta02")


    // permission for compose
    implementation("com.google.accompanist:accompanist-permissions:0.28.0")

    // ui controls
    implementation("com.google.accompanist:accompanist-systemuicontroller:0.28.0")


}