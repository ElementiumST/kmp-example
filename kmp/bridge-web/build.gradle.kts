plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.multiplatform")
    id("org.jetbrains.kotlin.plugin.serialization")
    alias(libs.plugins.ksp)
}

kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
        }
    }

    js(IR) {
        browser()
        binaries.library()
        generateTypeScriptDefinitions()
    }

    iosX64()
    iosArm64()
    iosSimulatorArm64()

    sourceSets {
        commonMain.dependencies {
            api(project(":kmp:core"))
            implementation(project(":kmp:tools:bridge-annotations"))
            implementation(libs.kotlinx.serialization.json)
        }
    }
}

dependencies {
    add("kspCommonMainMetadata", project(":kmp:tools:bridge-codegen"))
}

val syncBridgeTs by tasks.registering(Sync::class) {
    dependsOn("kspCommonMainKotlinMetadata")
    from(layout.buildDirectory.dir("generated/ksp/metadata/commonMain/resources/bridge/ts"))
    into(rootProject.layout.projectDirectory.dir("libs/src/lib/data-access-kmp-bridge/generated"))
    include("**/*.ts")
}

tasks.named("jsBrowserDevelopmentLibraryDistribution") {
    dependsOn(syncBridgeTs)
}

android {
    namespace = "com.example.kmpexample.kmp.bridge.web"
    compileSdk = 35

    defaultConfig {
        minSdk = 24
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}