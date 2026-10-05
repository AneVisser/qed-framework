plugins {
    kotlin("jvm") version "2.4.20"
    // Required for the publishing block below (publishToMavenLocal)
    `maven-publish`
}

group = "com.qed"
version = "1.0.0"

repositories {
    mavenCentral()
}

kotlin {
    jvmToolchain(17)
}

dependencies {
    implementation(kotlin("stdlib"))
    // api (not implementation): consumers of QED-Shared also need qed.contract types,
    // because the RequestType alias below points to them
    api("com.qed:QED-Api-Contract:1.0.0")
}

// Publishing — allows consumers (e.g. the test suite) to use this as a jar via mavenLocal
publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
        }
    }
}