import org.jetbrains.kotlin.gradle.plugin.mpp.apple.XCFramework

plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.multiplatform")
    id("org.jetbrains.kotlin.plugin.serialization")
}

val xcf = XCFramework("SharedCore")

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

    iosX64 {
        binaries.framework {
            baseName = "SharedCore"
            isStatic = true
            export(project(":kmp:feature:auth"))
            export(project(":kmp:feature:base"))
            xcf.add(this)
        }
    }

    iosArm64 {
        binaries.framework {
            baseName = "SharedCore"
            isStatic = true
            export(project(":kmp:feature:auth"))
            export(project(":kmp:feature:base"))
            xcf.add(this)
        }
    }

    iosSimulatorArm64 {
        binaries.framework {
            baseName = "SharedCore"
            isStatic = true
            export(project(":kmp:feature:auth"))
            export(project(":kmp:feature:base"))
            xcf.add(this)
        }
    }

    sourceSets {
        commonMain.dependencies {
            api(project(":kmp:data"))
            implementation(project(":kmp:domain"))
            api(project(":kmp:feature:auth"))
            api(project(":kmp:feature:base"))
            api(libs.decompose)
            implementation(libs.koin.core)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.serialization.json)
        }
    }
}

android {
    namespace = "com.example.kmpexample.kmp.core"
    compileSdk = 35

    defaultConfig {
        minSdk = 24
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}
