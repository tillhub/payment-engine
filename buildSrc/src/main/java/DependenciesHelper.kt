import org.gradle.api.artifacts.dsl.DependencyHandler

// Util functions for easier adding different type of dependencies

sealed class Dependency {
    abstract val configurationName: String
    abstract val dependencyNotation: String

    data class Implementation(override val dependencyNotation: String): Dependency() {
        override val configurationName: String
            get() = IMPLEMENTATION
    }
    data class Api(override val dependencyNotation: String): Dependency() {
        override val configurationName: String
            get() = API
    }
    data class Kapt(override val dependencyNotation: String): Dependency() {
        override val configurationName: String
            get() = KAPT
    }

    data class TestImplementation(override val dependencyNotation: String): Dependency() {
        override val configurationName: String
            get() = TEST_IMPLEMENTATION
    }

    companion object {
        private const val KAPT = "kapt"
        private const val IMPLEMENTATION = "implementation"
        private const val API = "api"
        private const val TEST_IMPLEMENTATION = "testImplementation"
    }
}


fun DependencyHandler.implementDependencyGroup(list: List<Dependency>) {
    list.forEach { dependency ->
        add(dependency.configurationName, dependency.dependencyNotation)
    }
}
