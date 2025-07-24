import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.compose.compiler)
}

// Load local.properties
val localProperties = Properties()
val localPropertiesFile = rootProject.file("local.properties")
if (localPropertiesFile.exists()) {
    localProperties.load(localPropertiesFile.inputStream())
}

android {
    namespace = "com.davidp799.patcotoday"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.davidp799.patcotoday"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "2.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }

        // For development, read from local.properties if available
        val apiKey = localProperties.getProperty("API_KEY") ?: "ZMz6Y9oU9I9nfPK2PHuR09zN4lYrFdG49it2znq2"
        buildConfigField("String", "API_KEY", "\"$apiKey\"")
    }

    buildTypes {
        debug {
            // Use local.properties for development
            val devApiKey = localProperties.getProperty("API_KEY") ?: "ZMz6Y9oU9I9nfPK2PHuR09zN4lYrFdG49it2znq2"
            buildConfigField("String", "API_KEY", "\"$devApiKey\"")
        }
        release {
            // Use the production API key for release builds
            buildConfigField("String", "API_KEY", "\"ZMz6Y9oU9I9nfPK2PHuR09zN4lYrFdG49it2znq2\"")
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("debug")
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
        buildConfig = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
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
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.material.icons.extended.android)

    // Add networking and API dependencies
    implementation(libs.retrofit)
    implementation(libs.converter.gson)
    implementation(libs.okhttp)
    implementation(libs.logging.interceptor)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.preference.ktx)
    implementation(libs.material)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.ui.tooling)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}