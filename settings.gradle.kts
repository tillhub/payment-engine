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
            val projectProperties = readProperties(file("$rootDir/local_secrets.properties"))
            url = URI.create("https://nexus.softpay.io/repository/softpay-external-sdk/")
            credentials {
                username = projectProperties["nexus_username"].toString()
                password = projectProperties["nexus_password"].toString()
            }
        }
    }
}

rootProject.name = "Tillhub Payment Engine"
include(":sample")
include(":payment-engine")
include(":payment-softpay")

fun readProperties(propertiesFile: File) = Properties().apply {
    propertiesFile.inputStream().use { fis ->
        load(fis)
    }
}