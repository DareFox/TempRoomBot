import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.6.20"
    kotlin("plugin.serialization") version "1.6.20"
    application
}

// TODO: Change group
group = "change.group"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven {
        url = uri("https://maven.kotlindiscord.com/repository/maven-public/")
    }
}

// Discord API version
val kordExtVersion = "1.5.2-RC1"

dependencies {
    testImplementation(kotlin("test"))

    // Discord API
    implementation("com.kotlindiscord.kord.extensions:kord-extensions:$kordExtVersion")

    // Logger
    implementation("ch.qos.logback:logback-classic:1.2.11")
    implementation("io.github.microutils:kotlin-logging-jvm:2.1.21")

    // MongoDB
    implementation("org.litote.kmongo:kmongo:4.5.1")
}

// Create fat-jar on build
tasks.withType<Jar> {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE

    manifest {
        attributes["Main-Class"] = "MainKt"
    }

    from(configurations.compileClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
}

// Test task
tasks.test {
    useJUnit()
}

// Heroku stuff
tasks.create("stage") {
    dependsOn("installDist")
}

// Compile to JVM verison...
tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

// Main-class (duh)
application {
    mainClass.set("MainKt")
}