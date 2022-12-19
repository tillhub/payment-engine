configurations.maybeCreate("saleSdk-debug")
configurations.maybeCreate("saleSdk-release")
configurations.maybeCreate("utils-debug")
configurations.maybeCreate("utils-release")
artifacts {
    add("saleSdk-debug", file("saleSdk-debug.aar"))
    add("saleSdk-release", file("saleSdk-release.aar"))
    add("utils-debug", file("utils-debug.aar"))
    add("utils-release", file("utils-release.aar"))
}
