import org.gradle.api.JavaVersion

object ConfigData {
    const val applicationId = "de.tillhub.paymentengine"
    const val minSdkVersion = 21
    const val targetSdkVersion = 32
    const val versionCode = 1
    const val versionName = "1.0.0"

    val JAVA_VERSION = JavaVersion.VERSION_11
    val JVM_TARGET = JavaVersion.VERSION_11.toString()

    object ArtifactId {
        const val DEBUG = "payment-engine-debug"
        const val RELEASE = "payment-engine"
    }

    object BuildType {
        const val DEBUG = "debug"
        const val RELEASE = "release"
    }
}
