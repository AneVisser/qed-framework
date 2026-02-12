plugins {
    kotlin("jvm") version "2.0.20"
}

group = "com.qed"
version = "1.0.0"

repositories {
    mavenCentral()
}

kotlin {
    jvmToolchain(22)
}

dependencies {
    implementation(kotlin("stdlib"))
}