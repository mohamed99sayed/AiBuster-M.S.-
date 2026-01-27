plugins {

    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.example.myapplication3"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.myapplication3"
        minSdk = 24
        targetSdk = 36
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
        mlModelBinding = true
        viewBinding = true
    }
}
// In your build.gradle.kts (Module :app) file

dependencies {
    // This constrains all libraries to use the version of Material Design
    // specified in your app, preventing conflicts.
    constraints {
        implementation("com.google.android.material:material:1.13.0") {
            because("Ensure all libraries use the same version of Material Components.")
        }
    }
// PyTorch Mobile Libraries
    implementation("org.pytorch:pytorch_android_lite:1.13.1")
    implementation("org.pytorch:pytorch_android_torchvision_lite:1.13.1")
    implementation("androidx.core:core-ktx:1.17.0")
    implementation("androidx.appcompat:appcompat:1.7.1")
    implementation("com.google.android.material:material:1.13.0") // Your main material dependency
    implementation("androidx.constraintlayout:constraintlayout:2.2.1")
    implementation("androidx.navigation:navigation-fragment-ktx:2.9.4")
    implementation("androidx.navigation:navigation-ui-ktx:2.9.4")
    implementation("androidx.databinding:viewbinding:8.13.0") // From your context
    implementation("androidx.activity:activity:1.11.0") // From your context
    implementation("androidx.fragment:fragment:1.8.9") // From your context
    implementation("androidx.recyclerview:recyclerview:1.4.0") // From your context
    implementation("androidx.legacy:legacy-support-v4:1.0.0") // From your context

    // --- TensorFlow Dependencies with Exclude Rule ---
    // The exclude rule prevents the library from bringing its own conflicting
    // material components into the build.
    implementation("org.tensorflow:tensorflow-lite-support:0.5.0") {
        exclude(group = "com.google.android.material")
    }
    implementation("org.tensorflow:tensorflow-lite-metadata:0.5.0") {
        exclude(group = "com.google.android.material")
    }
    implementation("org.tensorflow:tensorflow-lite:2.17.0")
    implementation("org.tensorflow:tensorflow-lite-gpu:2.17.0")


    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
}
