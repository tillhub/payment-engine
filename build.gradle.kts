import java.net.URI

plugins {
    kotlin(Dependencies.KotlinPlugins.ANDROID)
    kotlin(Dependencies.KotlinPlugins.KAPT)
    id(Dependencies.Plugins.LIBRARY)
    id(Dependencies.Plugins.DETEKT) version Versions.Plugins.DETEKT
    id(Dependencies.Plugins.HILT)
    id(Dependencies.Plugins.PARCELIZE)
    id(Dependencies.Plugins.PUBLISH)
}

repositories {
    google()
    mavenCentral()
    maven {
        url = URI.create("https://jitpack.io")
    }
    gradlePluginPortal()
}

android {
    compileSdk = ConfigData.targetSdkVersion

    defaultConfig {
        minSdk = ConfigData.minSdkVersion
        targetSdk = ConfigData.targetSdkVersion
    }

    buildFeatures {
        viewBinding = true
    }

    buildTypes {
        getByName(ConfigData.BuildType.DEBUG) {
            isMinifyEnabled = false
        }
        getByName(ConfigData.BuildType.RELEASE) {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = ConfigData.JAVA_VERSION
        targetCompatibility = ConfigData.JAVA_VERSION
    }

    tasks.withType<Test> {
        useJUnitPlatform()
    }

    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions {
            jvmTarget = ConfigData.JVM_TARGET
        }
    }

    testOptions {
        unitTests {
            isIncludeAndroidResources = true
        }
    }
}

dependencies {

    // Core Dependencies
    implementDependencyGroup(Dependencies.Groups.CORE)
    implementDependencyGroup(Dependencies.Groups.LIFECYCLE)

    // Module Specific dependencies
    implementation(Dependencies.AndroidX.CONSTRAINT_LAYOUT)

    // Utils
    implementDependencyGroup(Dependencies.Groups.PARSERS)
    implementation(Dependencies.Tools.TIMBER)

    // Lavego
    implementDependencyGroup(Dependencies.Groups.LAVEGO)

    api("com.github.lavego.sales-sdk:saleSdk:1.0.0")
    api("com.github.lavego.sales-sdk:utils:1.0.0")

    // Unit tests
    implementDependencyGroup(Dependencies.Groups.TEST_LIBRARIES)
    implementDependencyGroup(Dependencies.Groups.TEST_ROBOLECTRIC)
}

afterEvaluate {
    publishing {
        publications {
            create<MavenPublication>(ConfigData.ArtifactId.DEBUG) {
                groupId = ConfigData.applicationId
                artifactId = ConfigData.ArtifactId.DEBUG
                version = ConfigData.versionName

                from(components.getByName(ConfigData.BuildType.DEBUG))
            }
            create<MavenPublication>(ConfigData.ArtifactId.RELEASE) {
                groupId = ConfigData.applicationId
                artifactId = ConfigData.ArtifactId.RELEASE
                version = ConfigData.versionName

                from(components.getByName(ConfigData.BuildType.RELEASE))
            }
        }
    }
}
