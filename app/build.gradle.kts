plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.google.gms.google.services)
}

android {
    namespace = "com.example.hackhound"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.hackhound"
        minSdk = 28
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildFeatures {
        viewBinding = true
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
}

dependencies {
    // Core Android KTX library
    implementation("androidx.core:core-ktx:1.12.0")
    // AndroidX AppCompat library
    implementation("androidx.appcompat:appcompat:1.6.1")
    // Google Material Design Components library
    implementation("com.google.android.material:material:1.11.0")
    // AndroidX ConstraintLayout library
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    // AndroidX Activity library
    implementation("androidx.activity:activity:1.8.0")
    // AndroidX Navigation Fragment KTX library
    implementation("androidx.navigation:navigation-fragment-ktx:2.7.7")
    // AndroidX Navigation UI KTX library
    implementation("androidx.navigation:navigation-ui-ktx:2.7.7")
    // Android Car UI library
    implementation("com.android.car.ui:car-ui-lib:2.6.0")
    // Firebase Authentication library
    implementation("com.google.firebase:firebase-auth:23.0.0")
    // Firebase Realtime Database library
    implementation("com.google.firebase:firebase-database:21.0.0")
    // Firebase Storage library
    implementation("com.google.firebase:firebase-storage:21.0.0")
    implementation(libs.androidx.cardview)
    // JUnit testing framework
    testImplementation("junit:junit:4.13.2")
    // AndroidX JUnit extension for Android tests
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    // AndroidX Espresso testing framework
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    // Google Sign-In library
    implementation("com.google.android.gms:play-services-auth:20.5.0")
    // QR Scanner library
    implementation(libs.qr.scanner)
    // Glide library
    implementation("com.github.bumptech.glide:glide:4.16.0")
    annotationProcessor("com.github.bumptech.glide:compiler:4.12.0")


    implementation ("androidx.cardview:cardview:1.0.0")
    implementation ("com.google.android.material:material:1.9.0")



}
