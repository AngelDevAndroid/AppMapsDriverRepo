plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.google.gms.google.services)
    id("com.google.devtools.ksp")
}

android {
    namespace = "com.angandroid.appmapsdriver"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.angandroid.appmapsdriver"
        minSdk = 23
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
        viewBinding = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)

    //implementation (platform(libs.firebase.bom.v33100))
    implementation (platform(libs.firebase.bom))
    implementation (libs.firebase.firestore.ktx)
    implementation (libs.firebase.storage)


    // Maps
    implementation (libs.maps.ktx)
    implementation (libs.maps.utils.ktx)
    implementation (libs.android.maps.utils)
    implementation (libs.play.services.maps)
    implementation (libs.play.services.location)

    implementation (libs.easywaylocation)
    implementation (libs.geofirestore.android)

    implementation(libs.firebase.auth)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // Image picker
    implementation(libs.imagepicker)

    // Glide img
    implementation (libs.glide)

    // FCM
    implementation(libs.firebase.messaging)

    // DATA store
    implementation("androidx.datastore:datastore-preferences:1.1.4")

    // AIzaSyAokw_qBC9AB5ZEQl5JM7f3Q-vaGsyL_wE
}