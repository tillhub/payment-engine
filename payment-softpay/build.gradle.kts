import java.util.Properties

plugins {
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.kotlinAndroid)
    alias(libs.plugins.detekt)
    id("kotlin-parcelize")
    id("maven-publish")
}

android {
    namespace = "de.tillhub.paymentengine.softpay"
    compileSdk = Configs.COMPILE_SDK

    defaultConfig {
        minSdk = Configs.MIN_SDK_SOFTPAY

        val projectProperties = readProperties(file("$rootDir/local.properties"))
        buildConfigField("String", "ACCESS_ID", "\"${projectProperties["accessId"]}\"")
        buildConfigField("String", "ACCESS_SECRET", "\"${projectProperties["accessSecret"]}\"")
        buildConfigField("String", "INTEGRATOR_ID", "\"${projectProperties["integratorId"]}\"")
    }

    buildTypes {
        val projectProperties = readSecretsProperties()
        debug {
            buildConfigField("String", "ACCESS_ID", "\"${projectProperties["sandboxAccessId"]}\"")
            buildConfigField("String", "ACCESS_SECRET", "\"${projectProperties["sandboxAccessSecret"]}\"")
            buildConfigField("String", "INTEGRATOR_ID", "\"${projectProperties["ntegratorId"]}\"")

            isMinifyEnabled = false
        }
        val release by getting {
            buildConfigField("String", "ACCESS_ID", "\"${projectProperties["accessId"]}\"")
            buildConfigField("String", "ACCESS_SECRET", "\"${projectProperties["accessSecret"]}\"")
            buildConfigField("String", "INTEGRATOR_ID", "\"${projectProperties["integratorId"]}\"")

            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            consumerProguardFiles(
                "consumer-rules.pro"
            )
        }
        create("staging") {
            initWith(release)

            buildConfigField("String", "ACCESS_ID", "\"${projectProperties["sandboxAccessId"]}\"")
            buildConfigField("String", "ACCESS_SECRET", "\"${projectProperties["sandboxAccessSecret"]}\"")
            buildConfigField("String", "INTEGRATOR_ID", "\"${projectProperties["integratorId"]}\"")
        }
    }
    compileOptions {
        sourceCompatibility = Configs.JAVA_VERSION
        targetCompatibility = Configs.JAVA_VERSION
        isCoreLibraryDesugaringEnabled = true
    }
    kotlinOptions {
        jvmTarget = Configs.JVM_TARGET
        freeCompilerArgs = listOf("-Xjvm-default=all")
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

    // Unit tests
    testImplementation(libs.bundles.testing)
    testImplementation(libs.bundles.robolectric)

    // Softpay
    debugApi(libs.softpay.sdk.sandbox)
    releaseApi(libs.softpay.sdk)
}

afterEvaluate {
    publishing {
        publications {
            create<MavenPublication>("release-softpay") {
                groupId = Configs.APPLICATION_ID
                artifactId = "softpay"
                version = Configs.VERSION_CODE

                from(components.getByName("release"))
            }
            create<MavenPublication>("sandbox-softpay") {
                groupId = Configs.APPLICATION_ID
                artifactId = "softpay-sanbox"
                version = Configs.VERSION_CODE

                from(components.getByName("staging"))
            }
        }
        repositories {
            maven {
                url = uri("https://nexus.infra.unzer.io/repository/tillhub-payment-engine-hosted/")
                credentials {
                    username = System.getenv("NEXUS_USERNAME")
                    password = System.getenv("NEXUS_PASSWORD")
                }
            }
        }
    }
}

fun readSecretsProperties() = Properties().apply {
    val propertiesFile = file("$rootDir/local.properties")

    if (propertiesFile.exists()) {
        propertiesFile.inputStream().use { fis ->
            load(fis)
        }
    } else {
        put("sandboxAccessId", System.getenv("SOFTPAY_SANDBOX_ACCESS_ID"))
        put("sandboxAccessSecret", System.getenv("SOFTPAY_SANDBOX_ACCESS_SECRET"))

        put("accessId", System.getenv("SOFTPAY_ACCESS_ID"))
        put("accessSecret", System.getenv("SOFTPAY_ACCESS_SECRET"))
        put("integratorId", System.getenv("SOFTPAY_INTEGRATOR_ID"))
    }
}
