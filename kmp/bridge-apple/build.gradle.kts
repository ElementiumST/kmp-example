import org.jetbrains.kotlin.gradle.plugin.mpp.apple.XCFramework

plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.multiplatform")
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
    }

    iosX64 {
        binaries.framework {
            baseName = "SharedCore"
            isStatic = true
            export(project(":kmp:core"))
            export(project(":kmp:feature:auth"))
            export(project(":kmp:feature:contacts"))
            export(project(":kmp:feature:base"))
            export(project(":kmp:domain"))
            xcf.add(this)
        }
    }

    iosArm64 {
        binaries.framework {
            baseName = "SharedCore"
            isStatic = true
            export(project(":kmp:core"))
            export(project(":kmp:feature:auth"))
            export(project(":kmp:feature:contacts"))
            export(project(":kmp:feature:base"))
            export(project(":kmp:domain"))
            xcf.add(this)
        }
    }

    iosSimulatorArm64 {
        binaries.framework {
            baseName = "SharedCore"
            isStatic = true
            export(project(":kmp:core"))
            export(project(":kmp:feature:auth"))
            export(project(":kmp:feature:contacts"))
            export(project(":kmp:feature:base"))
            export(project(":kmp:domain"))
            xcf.add(this)
        }
    }

    sourceSets {
        commonMain.dependencies {
            api(project(":kmp:core"))
            api(project(":kmp:feature:auth"))
            api(project(":kmp:feature:contacts"))
            api(project(":kmp:feature:base"))
            api(libs.decompose)
        }
    }
}

android {
    namespace = "com.example.kmpexample.kmp.bridge.apple"
    compileSdk = 35

    defaultConfig {
        minSdk = 24
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}
