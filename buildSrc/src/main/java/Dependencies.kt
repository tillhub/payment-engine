
object Dependencies {

    object Plugins {
        const val LIBRARY = "com.android.library"
        const val DETEKT = "io.gitlab.arturbosch.detekt"
        const val HILT = "dagger.hilt.android.plugin"
        const val PARCELIZE = "kotlin-parcelize"
        const val PUBLISH = "maven-publish"
    }

    object KotlinPlugins {
        const val ANDROID = "android"
        const val KAPT = "kapt"
    }

    object Tools {
        const val TIMBER = "com.jakewharton.timber:timber:${Versions.Tools.TIMBER}"
    }

    object Kotlin {
        const val COROUTINES = "org.jetbrains.kotlinx:kotlinx-coroutines-android:${Versions.Kotlin.COROUTINES}"
        const val COROUTINES_TEST = "org.jetbrains.kotlinx:kotlinx-coroutines-test:${Versions.Kotlin.COROUTINES}"
    }

    object AndroidX {
        const val CORE_KTX = "androidx.core:core-ktx:${Versions.AndroidX.CORE_KTX}"
        const val APP_COMPAT = "androidx.appcompat:appcompat:${Versions.AndroidX.APP_COMPAT}"
        const val CONSTRAINT_LAYOUT = "androidx.constraintlayout:constraintlayout:${Versions.AndroidX.CONSTRAINT_LAYOUT}"

        const val ACTIVITY = "androidx.activity:activity-ktx:${Versions.AndroidX.ACTIVITY}"

        const val VIEW_MODEL = "androidx.lifecycle:lifecycle-viewmodel-ktx:${Versions.AndroidX.LIFECYCLE}"
        const val LIVE_DATA = "androidx.lifecycle:lifecycle-livedata-ktx:${Versions.AndroidX.LIFECYCLE}"
        const val LIFECYCLE_COMPILER = "androidx.lifecycle:lifecycle-compiler:${Versions.AndroidX.LIFECYCLE}"
        const val LIFECYCLE_RUNTIME = "androidx.lifecycle:lifecycle-runtime-ktx:${Versions.AndroidX.LIFECYCLE}"
    }

    object Google {
        const val GSON = "com.google.code.gson:gson:${Versions.Google.GSON}"
        const val HILT = "com.google.dagger:hilt-android:${Versions.Google.HILT}"
        const val HILT_COMPILER = "com.google.dagger:hilt-android-compiler:${Versions.Google.HILT}"
    }

    object Parsers {
        const val MOSHI = "com.squareup.moshi:moshi-kotlin:${Versions.Parsers.MOSHI}"
    }

    object Lavego {
        const val COMMONS_CODEC = "commons-codec:commons-codec:${Versions.Lavego.COMMONS_CODEC}"
        const val SIMPLE_XML = "org.simpleframework:simple-xml:${Versions.Lavego.SIMPLE_XML}"
    }

    object Testing {
        const val JUNIT = "junit:junit:${Versions.Testing.JUNIT}"
        const val MOCKK = "io.mockk:mockk:${Versions.Testing.MOCKK}"
        const val MOCKK_AGENT_JVM = "io.mockk:mockk-agent-jvm:${Versions.Testing.MOCKK}"
        const val ROBOLECTRIC = "org.robolectric:robolectric:${Versions.Testing.ROBOLECTRIC}"
        const val KOTEST_RUNNER = "io.kotest:kotest-runner-junit5:${Versions.Testing.KOTEST}"
        const val KOTEST_ASSERTIONS = "io.kotest:kotest-assertions-core:${Versions.Testing.KOTEST}"
        const val KOTEST_PROPERTY = "io.kotest:kotest-property:${Versions.Testing.KOTEST}"
        const val KOTEST_ROBOLECTRIC = "io.kotest.extensions:kotest-extensions-robolectric:${Versions.Testing.KOTEST_ROBOLECTRIC}"
    }

    object AndroidTesting {
        // Core library
        const val CORE = "androidx.test:core-ktx:${Versions.AndroidTest.CORE}"
    }

    object Groups {

        val CORE = arrayListOf<Dependency>().apply {
            add(Dependency.Implementation(AndroidX.CORE_KTX))
            add(Dependency.Implementation(Google.HILT))
            add(Dependency.Kapt(Google.HILT_COMPILER))
            add(Dependency.Implementation(Kotlin.COROUTINES))
            add(Dependency.Implementation(AndroidX.APP_COMPAT))
        }

        val LIFECYCLE = arrayListOf<Dependency>().apply {
            add(Dependency.Implementation(AndroidX.ACTIVITY))
            add(Dependency.Implementation(AndroidX.VIEW_MODEL))
            add(Dependency.Implementation(AndroidX.LIVE_DATA))
            add(Dependency.Implementation(AndroidX.LIFECYCLE_RUNTIME))
            add(Dependency.Kapt(AndroidX.LIFECYCLE_COMPILER))
        }

        val PARSERS = arrayListOf<Dependency>().apply {
            add(Dependency.Implementation(Parsers.MOSHI))
        }

        val LAVEGO = arrayListOf<Dependency>().apply {
            add(Dependency.Implementation(Lavego.COMMONS_CODEC))
            add(Dependency.Implementation(Lavego.SIMPLE_XML))
            add(Dependency.Implementation(Google.GSON))
        }

        val TEST_LIBRARIES = arrayListOf<Dependency>().apply {
            add(Dependency.TestImplementation(Testing.JUNIT))
            add(Dependency.TestImplementation(Testing.MOCKK))
            add(Dependency.TestImplementation(Testing.MOCKK_AGENT_JVM))
            add(Dependency.TestImplementation(Testing.KOTEST_RUNNER))
            add(Dependency.TestImplementation(Testing.KOTEST_ASSERTIONS))
            add(Dependency.TestImplementation(Testing.KOTEST_PROPERTY))
            add(Dependency.TestImplementation(Kotlin.COROUTINES_TEST))
        }

        val TEST_ROBOLECTRIC = arrayListOf<Dependency>().apply {
            add(Dependency.TestImplementation(AndroidTesting.CORE))
            add(Dependency.TestImplementation(Testing.ROBOLECTRIC))
            add(Dependency.TestImplementation(Testing.KOTEST_ROBOLECTRIC))
        }
    }
}
