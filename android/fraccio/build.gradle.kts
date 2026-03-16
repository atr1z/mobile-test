import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_21)
        }
    }
    
    sourceSets {
        androidMain.dependencies {
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.androidx.activity.compose)
        }
        commonMain.dependencies {
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.ui)
            implementation(libs.compose.components.resources)
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)
            implementation(projects.shared)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}

android {
    namespace = "mx.atriz"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "mx.atriz"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        getByName("debug") {
            applicationIdSuffix = ".debug"
            isDebuggable = true
            buildConfigField("String", "API_BASE_URL", "\"${System.getenv("FRACCIO_DEBUG_API_URL") ?: "http://10.0.2.2:8080"}\"")
            buildConfigField("Boolean", "ALLOW_ENV_OVERRIDE", "true")
        }
        create("dev") {
            applicationIdSuffix = ".dev"
            isDebuggable = true
            buildConfigField("String", "API_BASE_URL", "\"${System.getenv("FRACCIO_DEV_API_URL") ?: "https://dev.api.atriz.mx"}\"")
            buildConfigField("Boolean", "ALLOW_ENV_OVERRIDE", "false")
        }
        getByName("release") {
            isMinifyEnabled = false
            buildConfigField("String", "API_BASE_URL", "\"${System.getenv("FRACCIO_PROD_API_URL") ?: "https://api.atriz.mx"}\"")
            buildConfigField("Boolean", "ALLOW_ENV_OVERRIDE", "false")
        }
    }
    
    buildFeatures {
        buildConfig = true
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
}

dependencies {
    debugImplementation(libs.compose.uiTooling)
}

