plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.hector.driverdemo"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.hector.driverdemo"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "0.1-demo"

        vectorDrawables { useSupportLibrary = true }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            isDebuggable = true
        }
    }

    // Use Java 17 with AGP 8.x / Gradle 8.x
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
    }

    // Compose compiler that matches Compose 1.6.8 (via BOM 2024.04.01)
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.11"
    }

    // AGP 8.x syntax for packaging
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    // Compose BOM (keeps all compose libs aligned)
    val composeBom = platform("androidx.compose:compose-bom:2024.04.01")
    implementation(composeBom)
    androidTestImplementation(composeBom)

    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.activity:activity-compose:1.9.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.0")

    // Compose UI
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")

    // ✅ Add this (covers animateIntAsState, rememberInfiniteTransition, etc.)
    implementation("androidx.compose.animation:animation")

    // Icons (let BOM choose the right version)
    implementation("androidx.compose.material:material-icons-extended")

    // (Optional) Material Components for Views – harmless to keep
    implementation("com.google.android.material:material:1.12.0")

    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}
