import java.net.URI

pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.PREFER_SETTINGS)
    repositories {
        google()
        mavenCentral()
        maven {
            url = URI.create("https://jitpack.io")
        }
    }
}

rootProject.name = "Tillhub Payment Engine"
include(":sample")
include(":payment-engine")
include(":payment-softpay")
 