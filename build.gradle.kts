plugins {
    kotlin("jvm") version "1.8.0"
    kotlin("plugin.serialization") version "1.8.0" // For Kotlin serialization
    id("org.jetbrains.compose") version "1.5.0" // Compose plugin
    application
}

repositories {
    mavenCentral()
    google() // Wymagane dla Material3 w Compose
}

dependencies {
    implementation("io.ktor:ktor-client-core:2.0.0")
    implementation("io.ktor:ktor-client-cio:2.0.0")
    implementation("io.ktor:ktor-client-serialization:2.0.0")

    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.0")
    implementation("org.knowm.xchart:xchart:3.8.1")

    // Compose dependencies for Desktop
    implementation(compose.desktop.currentOs) // Core library for Compose for Desktop
    implementation("org.jetbrains.compose.material:material-icons-extended-desktop:1.5.0") // Opcjonalnie ikony Material
}

application {
    mainClass.set("MainKt")
}
