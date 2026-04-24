pluginManagement {
    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.PREFER_PROJECT)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "kmp-example"

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

include(":android:app")
include(":kmp:core")
include(":kmp:app")
include(":kmp:navigation")
include(":kmp:bridge-web")
include(":kmp:bridge-apple")
include(":kmp:data")
include(":kmp:domain")
include(":kmp:feature:base")
include(":kmp:feature:auth")
include(":kmp:feature:contacts")
include(":kmp:tools:bridge-annotations")
include(":kmp:tools:bridge-codegen")
include(":kmp:tools:mvi-annotations")
include(":kmp:tools:mvi-codegen")