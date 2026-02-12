Welcome to QED, a modern, Kotlin-based test automation framework designed for clarity, composability, and speed. This guide walks you through setting up QED from scratch — no prior experience required.

## Architecture Overview

QED uses a multi-repository architecture:

- **qed-framework** — The public, reusable test framework (includes `qed-demos/` as a working example)
- **SUT repositories** — Private repos for each System Under Test, each depending on the framework via Gradle composite builds

All repos are designed to sit as siblings on disk:
```
C:\QEDFramework\                     ← the framework (also the parent directory)
├── qed-demos\                       ← demo test suites (inside the framework repo)
├── QED-Shared\                      ← shared data classes
├── qed-sut-dairymax\                ← private SUT repo
├── qed-sut-dairymax-rest\           ← private SUT repo
├── qed-sut-guidewire\               ← private SUT repo
└── qed-sut-ollama\                  ← private SUT repo
```

## Prerequisites

Before you begin, make sure your system has:

**Java JDK 22+** — Install from [Amazon Corretto](https://aws.amazon.com/corretto/) or your preferred vendor. Check that `JAVA_HOME` is set to the correct directory in environment variables.

![img.png](img.png)

**IntelliJ IDEA** (Community Edition is fine) — Download from [JetBrains](https://www.jetbrains.com/idea/). Create the associations with `.kt`, `.gradle`, `.java`, `.kts` if you want these files to load automatically in IntelliJ.

![img_1.png](img_1.png)

**Git** — For cloning the repository (use all the default options).

![img_2.png](img_2.png)

Check in a CLI that Java and Git have been installed:
```shell
java -version
git --version
```

## Step 1: Clone the QED Framework

It may be that GitHub asks for credentials. Please follow the prompts and enter the credentials that were given.

```shell
git clone https://github.com/YOURUSERNAME/qed-framework.git QEDFramework
cd QEDFramework
```

## Step 2: Open the Framework in IntelliJ

Launch IntelliJ IDEA.

![img_3.png](img_3.png)

Select **Open** and choose the `QEDFramework` directory.
Click **Trust projects**.

In `File | Settings | Appearance`, you can choose the theme to your preference. In this documentation, the theme "Light" is chosen.

IntelliJ will detect the Gradle project and begin indexing.
If not, go to the right-hand toolbar in IntelliJ, and click on the **Gradle** button (1), and then on the **Sync** button (2).

![img_4.png](img_4.png)

That should install all dependencies and result in a successful build:

![img_7.png](img_7.png)

## Step 3: Configure Java & Gradle

**Set Project SDK:** Go to `File → Project Structure → Project`, and set the SDK to Java 22+.

![img_6.png](img_6.png)

**Gradle Settings:** Ensure Gradle uses the correct JVM:

`File → Settings → Build, Execution, Deployment → Build Tools → Gradle`

Use Gradle from: **Gradle wrapper**

![img_5.png](img_5.png)

## Step 4: Build the Framework

In IntelliJ (go to `View | Tool Windows | Terminal`):
```shell
gradlew clean build
```

## Step 5: Run a Demo Test

QED includes demo SUTs to help you validate your setup. The demos live inside the framework repository in the `qed-demos/` directory.

**Important:** The demos project is a separate Gradle project that uses a composite build to depend on the framework. Open it as a **separate IntelliJ project**:

1. Go to `File → Open`
2. Browse to `QEDFramework\qed-demos\`
3. Choose **New Window** when prompted
4. Wait for Gradle sync to complete

Now set up a run configuration. In IntelliJ, in the top bar, click on **Current File** and select **Edit Configurations**.

![img_8.png](img_8.png)

Then, click on the **+** in the top left corner of the dialog, and select **Gradle**.

![img_12.png](img_12.png)

The configuration should be set as follows:

- **Run:** `clean test -Ptestsuite=uitestingplayground -Penvironment=dev`
- **Gradle project:** `C:\QEDFramework\qed-demos`

![img_9.png](img_9.png)

Click on the **Run** button in the dialog. You can also click **OK**, and then launch the suite from the top bar.
The arrow button launches the suite, the bug button does the same but allows debugging.

![img_10.png](img_10.png)

The **Run** panel in IntelliJ should show that the tests are running. The first time around,
this is fairly slow as the Playwright dependencies need to be downloaded.

![img_13.png](img_13.png)

There will be one failure in the suite.
This was done on purpose to demonstrate how the report looks when a test fails.

![img_14.png](img_14.png)

If you see the browser launch and the tests execute, you're good to go. When the tests have finished, you can view the report
from `/build/test-output/ExtentReport/TestExecutionReport.html`.

![img_11.png](img_11.png)

Right-click on it and select **Open in | Browser | \<Favourite Browser\>**.
If you can view a report that looks like this, you have successfully run the demo suite.

![img_15.png](img_15.png)

## Available Demo Suites

Other demo suites can be run using the same configuration pattern, changing the `-Ptestsuite` parameter:

| Suite name | Run command |
|---|---|
| UI Testing Playground | `clean test -Ptestsuite=uitestingplayground -Penvironment=dev` |
| API Challenges | `clean test -Ptestsuite=apichallenges -Penvironment=dev` |
| Mixed UI/API | `clean test -Ptestsuite=mixedUIAPI -Penvironment=dev` |


---

## Creating a New SUT Repository

The `qed-demos` project serves as a template for creating your own SUT repositories. Follow these steps to set up a new test project.

### Step 1: Create the Directory Structure

Create a new directory alongside the framework:

```
C:\QEDFramework\qed-sut-mynewproject\
├── settings.gradle.kts
├── build.gradle.kts
├── resources\
│   └── fonts\
│       └── montserrat-v31-latin-regular.woff2    ← copy from framework
└── src\
    ├── main\
    │   └── kotlin\
    │       └── (your page objects, data classes, URL paths, etc.)
    └── test\
        └── kotlin\
            └── mynewproject\
                ├── mynewproject.xml               ← TestNG suite definition
                ├── mynewproject-config.json        ← test run configuration
                └── testcases\
                    └── (your test classes)
```

**Key convention:** The test subdirectory name, the XML filename, and the config JSON filename should all match the value you'll pass to `-Ptestsuite`.

### Step 2: Create settings.gradle.kts

```kotlin
rootProject.name = "qed-sut-mynewproject"

// ── Composite build ─────────────────────────────────────────────────
// Adjust the path to match your local directory layout.
//
includeBuild("../QEDFramework")

// Include QED-Shared if your project uses shared data classes
// includeBuild("../QEDFramework/QED-Shared")
```

**Note:** Since composite builds don't chain transitively, every SUT repo that depends on the framework must also include `QED-Shared` if the framework depends on it.

### Step 3: Create build.gradle.kts

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

    // Test dependencies (TestNG, Playwright, etc. come transitively from framework)
    testImplementation("org.jetbrains.kotlin:kotlin-test")

    // Add any project-specific dependencies here
}

// ── Test suite selection ─────────────────────────────────────────────
// Usage: ./gradlew clean test -Ptestsuite=mynewproject -Penvironment=dev
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

### Step 4: Create the TestNG Suite XML

Create `src/test/kotlin/mynewproject/mynewproject.xml`:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE suite SYSTEM "https://testng.org/testng-1.0.dtd">

<suite name="My New Project" verbose="10" parallel="methods" thread-count="5">
    <parameter name="configfile"
               value="src/test/kotlin/mynewproject/mynewproject-config.json"/>
    <test name="My Test Suite">
        <classes>
            <class name="testcases.MyFirstTest"/>
        </classes>
    </test>
</suite>
```

### Step 5: Create the Config JSON

Create `src/test/kotlin/mynewproject/mynewproject-config.json`:

```json
{
  "testrunmetadata": {
    "projectName": "My New Project",
    "environment": "dev",
    "baseURL": "https://your-app-url.com"
  }
}
```

Adjust the fields to match your project's configuration structure.

### Step 6: Create a .gitignore

Copy the `.gitignore` from any existing SUT repo, or create one with:

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

### Step 7: Write Your First Test

Create a page object in `src/main/kotlin/pages/`:

```kotlin
package pages

import qed.basepage.BasePage
import com.microsoft.playwright.Page

class MyLoginPage(page: Page) : BasePage(page) {
    // Define your page elements and actions
}
```

Create a test in `src/test/kotlin/mynewproject/testcases/`:

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

### Step 8: Open in IntelliJ and Run

1. Open the SUT directory as a **separate IntelliJ project** (`File → Open → New Window`)
2. Wait for Gradle sync to complete
3. Set up a run configuration:
    - **Run:** `clean test -Ptestsuite=mynewproject -Penvironment=dev`
    - **Gradle project:** point to your SUT directory
4. Run the suite

### Step 9: Push to GitHub

1. Create a new **private** repository on GitHub (empty, no README)
2. In your terminal or Sourcetree, initialize and push:

```shell
cd C:\QEDFramework\qed-sut-mynewproject
git init
git add .
git commit -m "Initial commit"
git remote add origin https://github.com/YOURUSERNAME/qed-sut-mynewproject.git
git branch -M main
git push -u origin main
```

Or in Sourcetree: create the repo, add the remote, stage all, commit, and push.

---

## Summary: Composite Build Pattern

Every SUT repository follows the same pattern:

| Component | Purpose |
|---|---|
| `settings.gradle.kts` | Names the project and points to the framework via `includeBuild` |
| `build.gradle.kts` | Declares the framework dependency and configures test suite selection |
| `src/main/kotlin/` | Page objects, data classes, URL paths — your SUT-specific code |
| `src/test/kotlin/<suite>/` | TestNG XML, config JSON, and test classes |
| `resources/fonts/` | Font file for Extent Reports |
| `.gitignore` | Excludes build artifacts and IDE files |

The `qed-demos` project inside the framework repository is the reference implementation of this pattern. When in doubt, compare your setup with demos.

---

## Sharing Data Classes Between a kTor App and QED Test Suites

When your System Under Test is a Kotlin-based kTor application, you can share data classes, route definitions, and interfaces between the application and its test suite. This eliminates double maintenance — if a field changes in the app, the test suite fails at compile time rather than at runtime.

### Architecture

QED uses a layered shared-library approach:

```
QED-Shared                      ← Generic interfaces (e.g. RequestType)
   ↑              ↑                 Part of the framework repo (public)
framework    QED-Shared-MyApp   ← App-specific data classes, routes, DTOs
   ↑              ↑         ↑       Separate private repo
SUT repo     kTor app       │
                             │
   (both consume the same compiled data classes)
```

**QED-Shared** lives inside the framework repository and contains only generic types that any kTor integration might need, such as `RequestType` (GET, POST, PUT, DELETE, PATCH).

**QED-Shared-MyApp** is a separate, private repository containing your application-specific data classes: request/response models, route definitions, DTOs, and any other types that both the kTor app and the test suite need to agree on.

### Why This Pattern?

Consider a typical kTor route that returns a list of farms:

```kotlin
// In your kTor app
get("/api/farms") {
    val farms = farmService.getAll()
    call.respond(farms)
}
```

The response is serialized to JSON using a `Farm` data class. Your test suite deserializes the same JSON back into a `Farm` object. If both sides define `Farm` independently, they can drift apart — a renamed field in the app silently breaks the test at runtime.

With QED-Shared-MyApp, both sides use the **same** `Farm` class:

```kotlin
// In QED-Shared-MyApp — used by both kTor app and test suite
@Serializable
data class Farm(
    val id: Int,
    val name: String,
    val region: String,
    val herdSize: Int
)
```

If a field is renamed or removed, both the app and the tests fail to compile immediately.

### Step 1: Set Up QED-Shared-MyApp

Create a new directory alongside the framework:

```
C:\QEDFramework\QED-Shared-MyApp\
├── settings.gradle.kts
├── build.gradle.kts
└── src\
    └── main\
        └── kotlin\
            └── (your shared data classes)
```

**settings.gradle.kts:**

```kotlin
rootProject.name = "QED-Shared-MyApp"

// ── Composite build ─────────────────────────────────────────────────
// QED-Shared provides generic types like RequestType
//
includeBuild("../QED-Shared")
```

Note: If QED-Shared lives inside the framework repo, adjust the path accordingly (e.g. `includeBuild("../QEDFramework/QED-Shared")`).

**build.gradle.kts:**

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
    // Use Java 17 for broad compatibility with kTor apps
    // The QED framework (Java 22) can consume Java 17 libraries without issues
    jvmToolchain(17)
}

dependencies {
    implementation(kotlin("stdlib"))

    // QED-Shared (resolved via composite build) — provides RequestType
    implementation("com.qed:QED-Shared:1.0.0")

    // Serialization — for @Serializable data classes
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.8.0")

    // Moshi — if your kTor app uses Moshi for JSON handling
    implementation("com.squareup.moshi:moshi:1.15.2")
    implementation("com.squareup.moshi:moshi-kotlin:1.15.2")
    implementation("com.squareup.moshi:moshi-adapters:1.15.2")
}
```

**JVM Toolchain note:** Set `jvmToolchain(17)` in QED-Shared and QED-Shared-MyApp if your kTor app runs on Java 17. The QED framework (Java 22) can consume libraries compiled with an older JDK without issues. Using the lowest common JVM version here avoids compatibility errors across consumers.

### Step 2: Organize Shared Data Classes

A typical structure for the shared classes:

```
src/main/kotlin/
├── routes/
│   ├── Routes.kt              ← Route enum with paths and RequestTypes
│   ├── RouteGroups.kt         ← Grouping of routes (e.g. AUTH, FARMS, ADMIN)
│   ├── Permission.kt          ← Permission definitions
│   └── Role.kt                ← Role definitions
├── ktor_responses/
│   ├── AuthResponse.kt        ← Login/auth response models
│   ├── FarmResponse.kt        ← Farm CRUD response models
│   ├── CompanyResponse.kt     ← Company response models
│   └── ...                    ← Other response/request models
└── dto/
    └── NutrientTables.kt      ← Domain-specific DTOs
```

**What belongs in the shared library:**
- Request and response data classes used in kTor route handlers
- Route definitions (paths, HTTP methods, permissions)
- Enums and interfaces referenced by both the app and the tests
- DTOs that cross the API boundary

**What does NOT belong here:**
- App-specific business logic or services
- Database entities or repository classes
- Test-specific utilities or assertions

### Step 3: Configure Your kTor App

**settings.gradle.kts** in your kTor project:

```kotlin
rootProject.name = "MyKtorApp"

// ── Composite builds ─────────────────────────────────────────────────
// Important: composite builds don't chain transitively.
// Every dependency must be declared explicitly.
//
includeBuild("C:/QEDFramework/QED-Shared")
includeBuild("C:/QEDFramework/QED-Shared-MyApp")
```

Or with relative paths if the kTor project is nearby:

```kotlin
includeBuild("../../QEDFramework/QED-Shared")
includeBuild("../../QEDFramework/QED-Shared-MyApp")
```

**build.gradle.kts** — add to your dependencies:

```kotlin
dependencies {
    // Shared data classes
    implementation("com.qed:QED-Shared:1.0.0")
    implementation("com.qed:QED-Shared-MyApp:1.0.0")

    // ... your other kTor dependencies
}
```

### Step 4: Configure Your SUT Test Repo

**settings.gradle.kts** in your SUT repo:

```kotlin
rootProject.name = "qed-sut-myapp"

// ── Composite builds ─────────────────────────────────────────────────
// All three are needed — composite builds don't chain transitively
//
includeBuild("../QEDFramework")
includeBuild("../QEDFramework/QED-Shared")
includeBuild("../QEDFramework/QED-Shared-MyApp")
```

**build.gradle.kts** — add the shared dependency:

```kotlin
dependencies {
    // QED Framework
    implementation("com.qed:qed-framework:1.0.0")

    // Shared data classes for your app
    implementation("com.qed:QED-Shared-MyApp:1.0.0")

    // Test dependencies
    testImplementation("org.jetbrains.kotlin:kotlin-test")
}
```

### Step 5: Use Shared Types in Tests

With the shared library in place, your test code can use the same types as the kTor app:

```kotlin
package testcases

import org.testng.annotations.Test
import qed.testbaseclass.BaseTest
import ktor_responses.Farm
import routes.Routes

class FarmTests : BaseTest() {

    @Test
    fun verifyGetFarms() {
        // The Farm data class is the same one the kTor app serializes
        val response = restClient.get(Routes.GET_MY_FARMS)
        val farms: List<Farm> = response.deserialize()

        // If a field was renamed in the app, this test fails at compile time
        assert(farms.first().name.isNotEmpty())
    }
}
```

### Composite Build Chaining: Important Note

Gradle composite builds do **not** resolve transitively. If Project A includes Project B, and Project B includes Project C, Project A does **not** automatically see Project C. Every consumer must explicitly declare all its composite build dependencies.

This means every `settings.gradle.kts` that needs a dependency must have its own `includeBuild` — even if another included build already depends on it. This is the most common pitfall when setting up the shared library pattern.

### Keeping QED-Shared-MyApp Private

Since QED-Shared-MyApp contains your application-specific data classes, keep it in a **private** GitHub repository. QED-Shared itself is part of the public framework repo and only contains generic types.

To push QED-Shared-MyApp:

```shell
cd C:\QEDFramework\QED-Shared-MyApp
git init
git add .
git commit -m "Initial commit: shared data classes for MyApp"
git remote add origin https://github.com/YOURUSERNAME/QED-Shared-MyApp.git
git branch -M main
git push -u origin main
```