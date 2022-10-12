plugins {
    kotlin("jvm") version "1.7.20"
    application
}

group = "io.github.wisplang"
version = "1.0-SNAPSHOT"

sourceSets {
    create("transpiler") {
        this.compileClasspath += sourceSets.main.get().compileClasspath
        this.runtimeClasspath += sourceSets.main.get().runtimeClasspath
    }
}

repositories {
    mavenCentral()
}

val transpilerImplementation by configurations
dependencies {
    transpilerImplementation( sourceSets.main.get().output )
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions.jvmTarget = "17"
}

application {
    mainClass.set("io.github.wisplang.MainKt")
}
