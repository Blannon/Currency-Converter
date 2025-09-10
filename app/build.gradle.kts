import java.util.Properties

plugins {
    alias(libs.plugins.ktlint)
    alias(libs.plugins.detekt)
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("org.jetbrains.kotlin.plugin.serialization") version "2.2.10"
    id("com.google.android.libraries.mapsplatform.secrets-gradle-plugin")
    alias(libs.plugins.google.firebase.appdistribution)
    alias(libs.plugins.google.gms.google.services)
}

android {
    namespace = "com.blannonnetwork.currencyconveter"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.blannonnetwork.currencyconveter"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }
    val localProperties = Properties()
    val localPropertiesFile = File(rootDir, "secret.properties")
    if (localPropertiesFile.exists() && localPropertiesFile.isFile) {
        localPropertiesFile.inputStream().use {
            localProperties.load(it)
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
            buildConfigField(
                "String",
                "EXCHANGE_RATE_API_KEY",
                "\"${localProperties.getProperty("EXCHANGE_RATE_API_KEY") ?: ""}\"",
            )
        }

        debug {
            buildConfigField(
                "String",
                "API_KEY",
                "\"${localProperties.getProperty("API_KEY") ?: ""}\"",
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
        buildConfig = true
        resValues = true
    }
}

ktlint {
    android.set(true)
    outputColorName.set("RED")
    additionalEditorconfig.set(
        mapOf(
            "ktlint_standard_function-naming" to "disabled",
            "ktlint_standard_parameter-list-wrapping" to "disabled",
            "ktlint_standard_argument-list-wrapping" to "disabled",
            "ktlint_standard_max-line-length" to "disabled",
        ),
    )
}

detekt {
    buildUponDefaultConfig = true
    allRules = false
    config.setFrom(files("${rootDir}\\detekt.yml"))
    baseline = file("${rootDir}\\detekt-baseline.xml")
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
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.compose.material3)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
    // Ktor
    implementation(libs.bundles.ktor)

// Koin
    implementation(libs.bundles.koin)

// Extended Icons
    implementation(libs.androidx.material.icons.extended)

// Navigation
    implementation(libs.androidx.navigation.compose)
    implementation(libs.kotlinx.serialization.json)

//    glance
    implementation(libs.androidx.glance.appwidget)
    implementation(libs.androidx.glance.material3)
    implementation(libs.androidx.datastore.preferences)

    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.kotlinx.coroutines.android)
    testImplementation(kotlin("test"))
}
