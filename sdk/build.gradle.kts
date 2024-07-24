plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("maven-publish")
}

android {
    namespace = "sk.trustpay.api.sdk"
    compileSdk = 34

    defaultConfig {
        minSdk = 26

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        debug {
            buildConfigField("String", "API_BASE_URL", "\"https://aapitest.trustpay.eu\"")
            buildConfigField("String", "MAPI_BASE_URL", "\"https://amapitest.trustpay.eu\"")
            isJniDebuggable = true
        }

        release {
            buildConfigField("String", "API_BASE_URL", "\"https://aapi.trustpay.eu\"")
            buildConfigField("String", "MAPI_BASE_URL", "\"https://amapi.trustpay.eu\"")

            isMinifyEnabled = true
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
    buildFeatures {
        buildConfig = true
        viewBinding = true
    }
    lint {
        disable += "Unused"
        ignoreWarnings = true
    }
}

publishing {
    publications {
        create<MavenPublication>("ReleaseAar") {
            groupId = "sk.trustpay.api"
            artifactId = "sdk-release"
            version = "0.1.1"
            afterEvaluate { artifact(tasks.getByName("bundleReleaseAar")) }
        }
        create<MavenPublication>("DebugAar") {
            groupId = "sk.trustpay.api"
            artifactId = "sdk-debug"
            version = "0.1.1"

            afterEvaluate {artifact(tasks.getByName("bundleDebugAar")) }
        }
    }
}


dependencies {
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("androidx.browser:browser:1.8.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("com.google.android.material:material:1.11.0")
    implementation("com.google.androidbrowserhelper:androidbrowserhelper:2.5.0")
    implementation("com.squareup.okhttp3:okhttp:4.11.0")
    implementation("com.google.code.gson:gson:2.10.1")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}