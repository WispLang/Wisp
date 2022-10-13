plugins {
    kotlin("jvm") version "1.7.20"
    application
}

group = "io.github.wisplang"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation( "org.jetbrains.kotlinx:kotlinx-cli:0.3.5" )
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions.jvmTarget = "17"
}

application {
    mainClass.set("io.github.wisplang.MainKt")
}
