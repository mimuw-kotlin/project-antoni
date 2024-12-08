plugins {
    kotlin("jvm") version "1.8.0"
    kotlin("plugin.serialization") version "1.8.0"
    id("org.jetbrains.compose") version "1.5.0"
    application
}

repositories {
    mavenCentral()
    google()
    maven("https://maven.pkg.jetbrains.space/public/p/lets-plot/maven")
}

dependencies {
    implementation("io.ktor:ktor-client-core:2.0.0")
    implementation("io.ktor:ktor-client-cio:2.0.0")
    implementation("io.ktor:ktor-client-serialization:2.0.0")

    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.0")

    implementation("org.jetbrains.lets-plot:lets-plot-kotlin-jvm:4.2.0")
    implementation("org.jetbrains.lets-plot:lets-plot-image-export:4.2.0")
    implementation("org.jetbrains.lets-plot:lets-plot-kotlin:4.2.0")

    implementation(compose.desktop.currentOs)
    implementation("org.jetbrains.compose.material:material-icons-extended-desktop:1.5.0")

    implementation("org.jfree:jfreechart:1.5.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")

    //implementation("org.jetbrains.compose.web:web-widgets:<latest_version>")

}

application {
    mainClass.set("MainKt")
}