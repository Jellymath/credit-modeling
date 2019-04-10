import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.3.21"
}

group = "edu.jellymath"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven { setUrl("http://dl.bintray.com/kyonifer/maven") }
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    compile("com.kyonifer", "koma-core-ejml", "0.12")
    compile("com.kyonifer", "koma-plotting", "0.12")
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}