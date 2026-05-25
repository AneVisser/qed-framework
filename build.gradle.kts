plugins {
    kotlin("jvm") version "2.0.20"
    `java-library`  // enables api vs implementation distinction
    kotlin("plugin.allopen") version "2.0.20"
}

group = "com.qed"
version = "1.0.0"

repositories {
    mavenCentral()
}

kotlin {
    jvmToolchain(22)
}

allOpen {
    annotation("org.testng.annotations.Test")
    annotation("org.testng.annotations.BeforeClass")
    annotation("org.testng.annotations.AfterClass")
    annotation("org.testng.annotations.BeforeMethod")
    annotation("org.testng.annotations.AfterMethod")
}

dependencies {
    // ── Core Kotlin ──────────────────────────────────────────────────
    api(kotlin("stdlib"))
    api(kotlin("reflect"))

    // ── Test framework ───────────────────────────────────────────────
    // api: SUT test classes extend BaseTest which exposes TestNG annotations
    api("org.testng:testng:7.11.0")
    api("org.uncommons:reportng:1.1.4")

    // ── Browser automation ───────────────────────────────────────────
    // api: SUT page objects use Playwright types (Page, Locator, etc.)
    api("com.microsoft.playwright:playwright:1.54.0")

    // ── REST / API testing ───────────────────────────────────────────
    // api: SUT test classes may use RestAssured directly
    api("io.rest-assured:kotlin-extensions:5.5.7")
    api("io.rest-assured:spring-mock-mvc-kotlin-extensions:5.5.7")
    api("io.rest-assured:spring-web-test-client-kotlin-extensions:5.5.6")

    // ── Assertions ───────────────────────────────────────────────────
    // api: SUT tests use expekt assertions directly
    api("com.winterbe:expekt:0.5.0")

    // ── JSON ─────────────────────────────────────────────────────────
    // api: SUT code may use Moshi for custom adapters / data classes
    api("com.squareup.moshi:moshi:1.15.2")
    api("com.squareup.moshi:moshi-kotlin:1.15.2")
    api("com.squareup.moshi:moshi-adapters:1.15.2")
    // candidate to tighten to implementation if only used inside framework's QEDJson/JsonVerify
    api("org.skyscreamer:jsonassert:2.0-rc1")

    // ── Reporting ────────────────────────────────────────────────────
    // api: SUT code may reference ExtentTest for custom logging
    api("com.aventstack:extentreports:5.1.2")

    // ── Logging ──────────────────────────────────────────────────────
    // candidate to tighten to implementation if SUT code only logs via framework's Logger
    api("io.github.oshai:kotlin-logging:7.0.0")
    api("ch.qos.logback:logback-classic:1.5.28")
    api("org.slf4j:slf4j-api:1.7.2")
    api("org.slf4j:slf4j-simple:1.7.2")

    // ── Coroutines ───────────────────────────────────────────────────
    // api: SUT tests may use coroutines for concurrent/performance tests
    api("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.1")
    api("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:1.10.1")

    // ── HTML generation (used in reports) ────────────────────────────
    // candidate to tighten to implementation if only used inside framework reports
    api("org.jetbrains.kotlinx:kotlinx-html-jvm:0.10.1")

    // ── Shared data classes ──────────────────────────────────────────────
    // api: Framework code references types like IURLPath from QED-Shared
    api("com.qed:QED-Shared:1.0.0")
}
