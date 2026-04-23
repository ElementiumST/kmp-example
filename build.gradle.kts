plugins {
    id("dev.nx.gradle.project-graph") version("0.1.15")
}
buildscript {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }

    dependencies {
        classpath("com.android.tools.build:gradle:8.7.1")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:2.1.21")
        classpath("org.jetbrains.kotlin.plugin.compose:org.jetbrains.kotlin.plugin.compose.gradle.plugin:2.1.21")
        classpath("org.jetbrains.kotlin.plugin.serialization:org.jetbrains.kotlin.plugin.serialization.gradle.plugin:2.1.21")
        classpath("app.cash.sqldelight:gradle-plugin:2.1.0")
    }
}

allprojects {
    apply {
        plugin("dev.nx.gradle.project-graph")
    }
}