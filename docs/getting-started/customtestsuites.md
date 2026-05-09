# Custom Test Suites

The `qed-demos` project serves as the reference implementation for creating your own SUT repositories. Follow these steps to set up a new test project.

---

## Step 1: Create the Directory Structure

Create a new directory alongside the framework:

```
C:\QEDFramework\qed-sut-myapp\
├── settings.gradle.kts
├── build.gradle.kts
├── resources\
│   └── fonts\
│       └── montserrat-v31-latin-regular.woff2    ← copy from framework
└── src\
    ├── main\
    │   └── kotlin\
    │       └── (page objects, data classes, URL paths, etc.)
    └── test\
        └── kotlin\
            └── myapp\
                ├── myapp.xml                      ← TestNG suite definition
                ├── myapp-config.json              ← test run configuration
                └── testcases\
                    └── (test classes)
```

> The test subdirectory name, XML filename, and config JSON filename should all match the value passed to `-Ptestsuite`.

---

## Step 2: Create `settings.gradle.kts`

```kotlin
rootProject.name = "qed-sut-myapp"

// ── Composite build ──────────────────────────────────────────────────
// Adjust the path to match your local directory layout.
//
includeBuild("../QEDFramework")

// Include QED-Shared if your project uses shared data classes
// includeBuild("../QEDFramework/QED-Shared")
```

> Composite builds do not chain transitively. Every SUT repo that depends on the framework must also explicitly include `QED-Shared` if it uses shared types.

---

## Step 3: Create `build.gradle.kts`

```kotlin
plugins {
    kotlin("jvm") version "2.0.20"
}

group = "com.qed.sut"
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

    testImplementation("org.jetbrains.kotlin:kotlin-test")

    // Add any project-specific dependencies here
}

// ── Test suite selection ─────────────────────────────────────────────
// Usage: ./gradlew clean test -Ptestsuite=myapp -Penvironment=dev
//
val env: String = project.findProperty("environment") as? String ?: "dev"

tasks.withType<Test> {
    if (project.hasProperty("testsuite")) {
        val testSuite = project.property("testsuite") as String
        useTestNG {
            useDefaultListeners = false
            suites("src/test/kotlin/$testSuite/$testSuite.xml")
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
```

> Don't forget to register this Gradle project in IntelliJ as described in [Installation](installation.md), Step 4.

---

## Step 4: Create the TestNG Suite XML

Create `src/test/kotlin/myapp/myapp.xml`:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE suite SYSTEM "https://testng.org/testng-1.0.dtd">

<suite name="My App" verbose="10" parallel="methods" thread-count="5">
    <parameter name="configfile"
               value="src/test/kotlin/myapp/myapp-config.json"/>
    <test name="My Test Suite">
        <classes>
            <class name="testcases.MyFirstTest"/>
        </classes>
    </test>
</suite>
```

---

## Step 5: Create the Config JSON

Create `src/test/kotlin/myapp/myapp-config.json`:

```json
{
  "testrunmetadata": {
    "projectName": "My App",
    "environment": "dev",
    "baseURL": "https://your-app-url.com"
  }
}
```

---

## Step 6: Create a `.gitignore`

```
.gradle/
build/
!gradle/wrapper/gradle-wrapper.jar
.kotlin/
.idea/
*.iml
out/
hs_err_pid*.log
replay_pid*.log
.DS_Store
Thumbs.db
test-output/
```

---

## Step 7: Write Your First Test

Create a page object in `src/main/kotlin/pages/`:

```kotlin
package pages

import qed.basepage.BasePage
import com.microsoft.playwright.Page

class MyLoginPage(page: Page) : BasePage(page) {
    // Define your page elements and actions
}
```

Create a test in `src/test/kotlin/myapp/testcases/`:

```kotlin
package testcases

import org.testng.annotations.Test
import qed.testbaseclass.BaseTest

class MyFirstTest : BaseTest() {

    @Test
    fun verifyLoginPage() {
        // Your test logic here
    }
}
```

---

## Step 8: Open in IntelliJ and Run

1. Open the SUT directory as a separate IntelliJ project (`File → Open → New Window`)
2. Wait for Gradle sync to complete
3. Set up a run configuration:
    - **Run:** `clean test -Ptestsuite=myapp -Penvironment=dev`
    - **Gradle project:** point to your SUT directory
4. Run the suite

---

## Step 9: Push to GitHub

1. Create a new **private** repository on GitHub (empty, no README)
2. Initialise and push:

```shell
cd C:\QEDFramework\qed-sut-myapp
git init
git add .
git commit -m "Initial commit"
git remote add origin git@github.com:YourUsername/qed-sut-myapp.git
git branch -M main
git push -u origin main
```

---

## Project Structure Summary

| Component | Purpose |
|-----------|---------|
| `settings.gradle.kts` | Names the project and wires in the framework via `includeBuild` |
| `build.gradle.kts` | Declares dependencies and configures test suite selection |
| `src/main/kotlin/` | Page objects, data classes, URL paths |
| `src/test/kotlin/<suite>/` | TestNG XML, config JSON, and test classes |
| `resources/fonts/` | Font file for Extent Reports |
| `.gitignore` | Excludes build artefacts and IDE files |

The `qed-demos` project inside the framework repository is the reference implementation of this pattern. When in doubt, compare your setup against it.

---

## Sharing Data Classes with a Ktor Application

When your system under test is a Kotlin-based Ktor application, you can share data classes, route definitions, and interfaces between the application and its test suite. This eliminates double maintenance — if a field changes in the app, the test suite fails at compile time rather than silently at runtime.

### Architecture

```
QED-Shared                      ← Generic interfaces (e.g. RequestType)
   ↑              ↑               Part of the framework repo (public)
framework    QED-Shared-MyApp   ← App-specific data classes, routes, DTOs
   ↑              ↑               Separate private repo
SUT repo     Ktor app
```

**QED-Shared** lives inside the framework repository and contains only generic types such as `RequestType`.

**QED-Shared-MyApp** is a separate private repository containing your application-specific data classes — request and response models, route definitions, DTOs, and any other types that both the Ktor app and the test suite need to share.

### Why This Pattern?

If both the Ktor app and the test suite define a `Farm` data class independently, they can drift apart — a renamed field in the app silently breaks the test at runtime. With a shared library, both sides use the **same** class:

```kotlin
@Serializable
data class Farm(
    val id: Int,
    val name: String,
    val region: String,
    val herdSize: Int
)
```

If a field is renamed or removed, both the app and the tests fail to compile immediately.

### Setting Up QED-Shared-MyApp

Create a new directory alongside the framework:

```
C:\QEDFramework\QED-Shared-MyApp\
├── settings.gradle.kts
├── build.gradle.kts
└── src\
    └── main\
        └── kotlin\
            └── (shared data classes)
```

**`settings.gradle.kts`:**

```kotlin
rootProject.name = "QED-Shared-MyApp"

includeBuild("../QED-Shared")
```

**`build.gradle.kts`:**

```kotlin
plugins {
    kotlin("jvm") version "2.0.20"
    kotlin("plugin.serialization") version "2.0.20"
}

group = "com.qed"
version = "1.0.0"

repositories {
    mavenCentral()
}

kotlin {
    // Use Java 17 for broad compatibility with Ktor apps.
    // The QED framework (Java 22) can consume Java 17 libraries without issues.
    jvmToolchain(17)
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation("com.qed:QED-Shared:1.0.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.8.0")
    implementation("com.squareup.moshi:moshi:1.15.2")
    implementation("com.squareup.moshi:moshi-kotlin:1.15.2")
    implementation("com.squareup.moshi:moshi-adapters:1.15.2")
}
```

### What Belongs in the Shared Library

- Request and response data classes used in Ktor route handlers
- Route definitions (paths, HTTP methods, permissions)
- Enums and interfaces referenced by both the app and the tests
- DTOs that cross the API boundary

What does **not** belong here: app-specific business logic, database entities, or test-specific utilities.

### Configuring the Ktor App

**`settings.gradle.kts`** in your Ktor project:

```kotlin
rootProject.name = "MyKtorApp"

includeBuild("../../QEDFramework/QED-Shared")
includeBuild("../../QEDFramework/QED-Shared-MyApp")
```

**`build.gradle.kts`:**

```kotlin
dependencies {
    implementation("com.qed:QED-Shared:1.0.0")
    implementation("com.qed:QED-Shared-MyApp:1.0.0")
    // ... other Ktor dependencies
}
```

### Configuring the SUT Repo

**`settings.gradle.kts`:**

```kotlin
rootProject.name = "qed-sut-myapp"

includeBuild("../QEDFramework")
includeBuild("../QEDFramework/QED-Shared")
includeBuild("../QEDFramework/QED-Shared-MyApp")
```

> **Important:** Gradle composite builds do not resolve transitively. Every consumer must explicitly declare all its `includeBuild` dependencies — even if another included build already depends on them. This is the most common pitfall when setting up the shared library pattern.

### Keeping QED-Shared-MyApp Private

Since QED-Shared-MyApp contains application-specific data classes, keep it in a private GitHub repository. QED-Shared itself is part of the public framework repo and contains only generic types.