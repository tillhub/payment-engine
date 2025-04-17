plugins {
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.kotlinAndroid)
    alias(libs.plugins.detekt)
    id("kotlin-parcelize")
    id("maven-publish")
}

android {
    namespace = "de.tillhub.paymentengine.softpay"
    compileSdk = 34

    defaultConfig {
        minSdk = 24
    }

    buildTypes {
        debug {
            isMinifyEnabled = false
        }
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            consumerProguardFiles(
                "consumer-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
        isCoreLibraryDesugaringEnabled = true
    }
    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_17.toString()
        freeCompilerArgs = listOf("-Xjvm-default=all")
    }
    buildFeatures {
        viewBinding = true
    }
    tasks.withType<Test> {
        useJUnitPlatform()
    }

    testOptions {
        unitTests {
            isIncludeAndroidResources = true
        }
    }
}

detekt {
    buildUponDefaultConfig = true // preconfigure defaults
    allRules = false // activate all available (even unstable) rules.
    config.setFrom("$rootDir/payment-engine/config/detekt.yml")
}

dependencies {
    implementation(project(":payment-engine"))

    // Core Dependencies
    implementation(libs.core.ktx)
    implementation(libs.bundles.lifecycle)
    implementation(libs.bundles.ui)
    coreLibraryDesugaring(libs.android.desugarJdkLibs)

    // Utils
    implementation(libs.moshi)
    implementation(libs.timber)
    detektPlugins(libs.detekt.formatting)
    detektPlugins(libs.detekt.libraries)

    // Unit tests
    testImplementation(libs.bundles.testing)
    testImplementation(libs.bundles.robolectric)

    // Softpay
    //debugImplementation(libs.softpay.sdk.sandbox)
    releaseImplementation(libs.softpay.sdk)
}

afterEvaluate {
    publishing {
        publications {
            create<MavenPublication>("payment-engine-softpay") {
                groupId = "de.tillhub.paymentengine.softpay"
                artifactId = "payment-engine:softpay"
                version = "3.4.1"

                from(components.getByName("release"))
            }
        }
    }
}