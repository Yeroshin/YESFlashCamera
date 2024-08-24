plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
}

android {
    namespace = "com.yes.flashcamera"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.yes.flashcamera"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
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

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildToolsVersion = "34.0.0"

    buildFeatures {
        viewBinding = true
        compose = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.appcompat)
    implementation(libs.play.services.basement)
    implementation(libs.androidx.camera.viewfinder)
    implementation(libs.sceneform.base)
    implementation(libs.androidx.navigation.runtime.ktx)
    implementation(libs.androidx.navigation.compose)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    //camerax
    implementation("androidx.camera:camera-core:1.2.2")
    implementation("androidx.camera:camera-camera2:1.2.2")
    implementation("androidx.camera:camera-lifecycle:1.2.2")
    implementation("androidx.camera:camera-video:1.2.2")

    implementation("androidx.camera:camera-view:1.2.2")
    implementation("androidx.camera:camera-extensions:1.2.2")

    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.2")
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)




    // Kotlin lang
    implementation("androidx.core:core-ktx:1.2.0")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.2.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.3.4")

    // App compat and UI things
    implementation("androidx.appcompat:appcompat:1.1.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.2.0")
    implementation("androidx.viewpager2:viewpager2:1.0.0")
    implementation("androidx.constraintlayout:constraintlayout:1.1.3")

    //adapter delegate
    implementation ("com.hannesdorfmann:adapterdelegates4-kotlin-dsl-viewbinding:4.3.2")

}