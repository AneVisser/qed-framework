plugins {
    kotlin("jvm") version "2.0.20"
}

group = "com.qed.qed-demos"
version = "1.0.0"

repositories {
    mavenCentral()
}

kotlin {
    jvmToolchain(22)
}

dependencies {
    // QED Framework (resolved via composite build)
    implementation("com.qed:qed-framework:1.0.0")

    // Test dependencies (TestNG, Playwright, etc. come transitively from framework)
    testImplementation("org.jetbrains.kotlin:kotlin-test")
}

// ── Test suite selection ─────────────────────────────────────────────
// Usage: ./gradlew clean test -Ptestsuite=apichallenges -Penvironment=dev
//
val env: String = project.findProperty("environment") as? String ?: "dev"

tasks.withType<Test> {
    if (project.hasProperty("testsuite")) {
        val testSuite = project.property("testsuite") as String
        val rootDir = when (testSuite) {
            "mixedUIAPI"          -> "mixedUIAPI"
            "uitestingplayground" -> "E2E_UITestingPlayground"
            "apichallenges"       -> "APIChallenges"
            else -> throw GradleException("Unknown demo test suite: $testSuite")
        }
        useTestNG {
            useDefaultListeners = false
            suites("src/test/kotlin/$rootDir/$testSuite.xml")
        }
    }
    systemProperty("env.name", env)
    testLogging {
        events("passed", "skipped", "failed")
    }
}

tasks.register<Copy>("copyExtentFonts") {
    from("resources/fonts")
    into(layout.buildDirectory.dir("test-output/ExtentReport/fonts"))
}

tasks.named("test") {
    finalizedBy("copyExtentFonts")
}