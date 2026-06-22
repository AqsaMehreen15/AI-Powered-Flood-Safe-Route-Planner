import java.util.Properties
import java.io.FileInputStream

val localProperties = Properties()
val localPropertiesFile = rootProject.file("local.properties")
if (localPropertiesFile.exists()) {
    localProperties.load(FileInputStream(localPropertiesFile))
}

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    // ✅ Firebase Google Services plugin applied securely for Kotlin DSL
    alias(libs.plugins.google.services)
}

android {
    namespace = "com.anzla.ai_poweredflood_saferouteplanner"
    compileSdk = 34

    buildFeatures {
        buildConfig = true
    }
    defaultConfig {
    applicationId = "com.anzla.ai_poweredflood_saferouteplanner"
    minSdk = 24
    targetSdk = 34
    versionCode = 1
    versionName = "1.0"
    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

    buildConfigField(
        "String",
        "OPENWEATHER_API_KEY",
        "\"${localProperties.getProperty("OPENWEATHER_API_KEY", "")}\""
    )
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
    buildToolsVersion = "34.0.0"
    packaging {
        jniLibs {
            useLegacyPackaging = false
        }
    }
}

dependencies {
    // Shared Preferences Configuration
    implementation("androidx.preference:preference-ktx:1.2.1")

    // Standard Android Libraries from Version Catalog
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)

    // ✅ FIXED: Aapke libs.versions.toml ke exact naming convention ke mutabiq
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)

    // OSM Android Maps and Locations
    implementation(libs.osmdroid.android)

    // Google Play Services Maps & Location
    implementation("com.google.android.gms:play-services-maps:18.2.0")
    implementation(libs.play.services.location)

    // MPAndroidChart (Graphs Optimization)
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")

    // TensorFlow Lite Core Packages
    implementation("com.google.ai.edge.litert:litert:1.0.1")
    implementation("com.google.ai.edge.litert:litert-support:1.0.1")
    implementation("com.google.ai.edge.litert:litert-api:1.0.1")

    // Firebase Artifacts
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.auth)

    // Testing Environment Configurations
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    implementation("com.github.MKergall:osmbonuspack:6.9.0")

}