import java.net.URI

pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven {
            url = URI.create("https://jitpack.io")
        }
        maven {
            url = uri("https://nexus.infra.unzer.io/repository/tillhub-payment-engine-hosted/")

            credentials {
                username = System.getenv("NEXUS_USERNAME")
                password = System.getenv("NEXUS_PASSWORD")
            }
        }
    }
}

rootProject.name = "Tillhub Payment Engine"
include(":sample")
include(":payment-engine")
 