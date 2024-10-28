import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    kotlin("kapt")
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.firebase.plugin)
    alias(libs.plugins.crashlytics)
}

android {
    namespace = libs.versions.projectApplicationId.get()
    compileSdk = libs.versions.projectCompileSdkVersion.get().toInt()

    defaultConfig {
        applicationId = libs.versions.projectApplicationId.get()
        minSdk = libs.versions.projectMinSdkVersion.get().toInt()
        targetSdk = libs.versions.projectTargetSdkVersion.get().toInt()
        versionCode = libs.versions.projectVersionCode.get().toInt()
        versionName = libs.versions.projectVersionName.get()

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }

        buildConfigField ("String", "BASE_URL", "\"http://77.237.234.47:3000/\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
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
        kotlinCompilerExtensionVersion = "1.5.9"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
//            excludes += "META-INF/INDEX.LIST"
//            excludes += "META-INF/LICENSE"
//            excludes += "META-INF/LICENSE.txt"
//            excludes += "META-INF/NOTICE"
//            excludes += "META-INF/NOTICE.txt"
//            excludes += "META-INF/DEPENDENCIES"
//            excludes += "META-INF/io.netty.versions.properties"
        }
    }
}

// === MY CUSTOM GRADLE TASKS ===

// Build counter:
val counterFile = file("$projectDir/src/main/assets/build_counter.properties")
tasks.register("incrementBuildCounter") {
    doLast {
        if (!counterFile.exists()) {
            counterFile.createNewFile()
        }

        val props = Properties().apply {
            counterFile.inputStream().use { load(it) }
        }

        val counter = (props.getProperty("buildCounter")?.toInt() ?: 0) + 1
        props.setProperty("buildCounter", counter.toString())

        counterFile.outputStream().use { props.store(it, null) }
    }
}

// Run my tasks before build:
tasks.named("preBuild") {
    dependsOn("incrementBuildCounter")
}

dependencies {

    // Core:
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)

    // Compose:
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.android)
    implementation(libs.firebase.config.ktx)
    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.firebase.messaging.ktx)
    implementation(libs.androidx.runtime.livedata)
    implementation(libs.play.services.ads.lite)

    // Test:
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    // Glide for image loading and caching:
    implementation (libs.glide)
    implementation(libs.glide.compose)

    // Dexter for permission handling:
    implementation (libs.dexter)

    // For the new type-safe navigation:
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.androidx.navigation.compose)

    // Ktor for networking:
    implementation(libs.ktor.client.auth)
    implementation(libs.ktor.client.cio)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.logging)
    implementation(libs.ktor.serialization.kotlinx.json)

    // Splash screen
    implementation(libs.androidx.core.splashscreen)

    // Firebase:
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.auth.ktx)
    implementation(libs.crashlytics)
    implementation(libs.analytics)
    implementation (libs.play.services.auth)

    // Swipe to reveal
    implementation (libs.swipe)
    //implementation (libs.revealswipe)

    // XML Photo Editor
    implementation (libs.photoeditor)

    // Retrofit
    implementation(libs.retrofit)
//    implementation (libs.kotlinx.coroutines.core)
//    implementation (libs.kotlinx.coroutines.android)
    implementation (libs.converter.gson)

    // Amazon S3
//    implementation(libs.s3)
    implementation("aws.sdk.kotlin:s3:1.0.0")

    // Shimmer
    implementation ("com.facebook.shimmer:shimmer:0.5.0")
    implementation("com.valentinilk.shimmer:compose-shimmer:1.3.1")


    val room_version = "2.6.1"
    implementation("androidx.room:room-runtime:$room_version") // Room runtime
    kapt("androidx.room:room-compiler:$room_version") // Use kapt for annotation processing in Kotlin
    implementation("androidx.room:room-ktx:$room_version") // For coroutines support

    // swipe to refress
    implementation ("com.google.accompanist:accompanist-swiperefresh:0.24.13-rc")

    // crop image
    implementation ("com.github.yalantis:ucrop:2.2.8")

    // Ads
    //implementation("com.google.android.gms:play-services-location:21.3.0")

    // Consents
    implementation("com.google.android.ump:user-messaging-platform:3.0.0")

    // Reviews
    implementation ("com.google.android.play:review:2.0.2")
    implementation ("com.google.android.play:review-ktx:2.0.2")

    // Billing
    implementation("com.android.billingclient:billing-ktx:7.1.1")

    implementation("org.jetbrains.kotlinx:kotlinx-collections-immutable:0.3.8")

}