import java.net.URI
import java.util.Properties

pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven {
            url = URI.create("https://jitpack.io")
        }
        maven {
            val nexusProperties = readNexusProperties()
            url = URI.create("https://nexus.softpay.io/repository/softpay-external-sdk/")
            credentials {
                username = nexusProperties["nexus_username"].toString()
                password = nexusProperties["nexus_password"].toString()
            }
        }
    }
}

rootProject.name = "Tillhub Payment Engine"
include(":sample")
include(":payment-engine")
include(":payment-softpay")

fun readNexusProperties() = Properties().apply {
    val propertiesFile = file("$rootDir/local.properties")

    if (propertiesFile.exists()) {
        propertiesFile.inputStream().use { fis ->
            load(fis)
        }
    } else {
        this["nexus_username"] = System.getenv("SOFTPAY_NEXUS_USERNAME")
        this["nexus_password"] = System.getenv("SOFTPAY_NEXUS_PASSWORD")
    }
}