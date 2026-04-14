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
include(":kmp:data")
include(":kmp:domain")
include(":kmp:feature:base")
include(":kmp:feature:auth")
