plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.kotlin.serialization)
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}
kotlin {
    compilerOptions {
        jvmTarget = org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17
    }
}

android {
    namespace = "io.nightfish.lightnovelreader.api"
    defaultConfig {
        multiDexEnabled = true
        minSdk = 24
    }
    compileSdk = 36

    buildFeatures {
        buildConfig = false
        compose = true
    }

    compileOptions {
        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    implementation(libs.androidx.foundation)
    implementation(libs.compose.ui.graphics)
    coreLibraryDesugaring(libs.desugar.jdk.libs)
    implementation(libs.androidx.runtime)
    implementation(libs.kotlinx.coroutines.core)
    implementation(platform(libs.compose.bom))
    implementation(libs.navigation.compose)
    implementation(libs.compose.material3)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.dom4j)
}
