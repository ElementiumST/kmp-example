plugins {
    id("org.jetbrains.kotlin.jvm")
}

dependencies {
    implementation(libs.ksp.symbol.processing.api)
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}