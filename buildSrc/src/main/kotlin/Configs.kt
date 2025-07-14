import org.gradle.api.JavaVersion

object Configs {
    const val APPLICATION_ID = "de.tillhub.paymentengine"
    const val COMPILE_SDK = 34
    const val MIN_SDK = 24
    const val MIN_SDK_SOFT_PAY = 26
    const val VERSION_CODE = "3.7.0"
    val JAVA_VERSION = JavaVersion.VERSION_17
    val JVM_TARGET = JAVA_VERSION.toString()
}
