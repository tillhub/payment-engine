plugins {
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.kotlinAndroid)
    alias(libs.plugins.detekt)
    id("kotlin-parcelize")
    id("maven-publish")
}

android {
    namespace = Configs.APPLICATION_ID + ".spos"
    compileSdk = Configs.COMPILE_SDK

    defaultConfig {
        minSdk = Configs.MIN_SDK
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = Configs.JAVA_VERSION
        targetCompatibility = Configs.JAVA_VERSION
        isCoreLibraryDesugaringEnabled = true
    }
    kotlinOptions {
        jvmTarget = Configs.JVM_TARGET
    }
    buildFeatures {
        viewBinding = true
    }
    tasks.withType<Test> {
        useJUnitPlatform()
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

    // XML
    implementation(libs.retrofit.simplexml) {
        exclude(module = "stax")
        exclude(module = "stax-api")
        exclude(module = "xpp3")
    }

    // Unit tests
    testImplementation(libs.bundles.testing)
    testImplementation(libs.bundles.robolectric)
}

afterEvaluate {
    publishing {
        publications {
            create<MavenPublication>("release-spos") {
                groupId = Configs.APPLICATION_ID
                artifactId = "spos"
                version = Configs.VERSION_CODE

                from(components.getByName("release"))
            }
        }
//        repositories {
//            maven {
//                url = uri("https://nexus.infra.unzer.io/repository/tillhub-payment-engine-hosted/")
//                credentials {
//                    username = System.getenv("NEXUS_USERNAME")
//                    password = System.getenv("NEXUS_PASSWORD")
//                }
//            }
//        }
    }
}