plugins {
//    alias(libs.plugins.kotlin.kapt)
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("org.jetbrains.kotlin.kapt")
    id("androidx.navigation.safeargs.kotlin")

}

android {
    namespace = "com.example.epi"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.epi"
        minSdk = 30
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

    buildFeatures {
        viewBinding=true
    }
}

dependencies {

    configurations.all {
        resolutionStrategy.eachDependency {
            if (requested.group == "com.intellij" && requested.name == "annotations") {
                useTarget("org.jetbrains:annotations:23.0.0")
            }
        }
    }
    implementation ("androidx.core:core-splashscreen:1.0.1")
    implementation ("com.google.code.gson:gson:2.10.1")

    implementation ("androidx.room:room-runtime:2.7.2")
    implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

    implementation ("com.google.code.gson:gson:2.10.1")

    implementation ("org.apache.poi:poi-ooxml:5.2.3")
    implementation ("org.apache.poi:poi:5.2.3")

    implementation("androidx.room:room-ktx:2.7.2")
    kapt("androidx.room:room-compiler:2.7.2")
    implementation("androidx.room:room-compiler:2.7.2")

    implementation("com.google.android.material:material:1.12.0")

    implementation("androidx.navigation:navigation-fragment-ktx:2.9.1")
    implementation("androidx.navigation:navigation-ui-ktx:2.9.1")

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}