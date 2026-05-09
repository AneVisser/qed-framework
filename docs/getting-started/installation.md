# Installation

Welcome to QED — a modern, Kotlin-based test automation framework designed for clarity, composability, and speed. This guide walks you through setting up QED from scratch.

---

## Architecture Overview

QED uses a multi-repository architecture:

- **qed-framework** — the public, reusable test framework (includes `qed-demos/` as a working example)
- **QED-Shared** — shared data classes used across the framework and SUT repositories
- **SUT repositories** — private repos for each System Under Test, each depending on the framework via Gradle composite builds

All repos are designed to sit as siblings on disk:

```
C:\QEDFramework\
├── qed-framework\          ← the framework
├── qed-demos\              ← demo test suites (inside the framework repo)
├── QED-Shared\             ← shared data classes
├── qed-sut-myapp\          ← your private SUT repo
└── qed-sut-myapp-rest\     ← your private REST SUT repo
```

---

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

---

## Step 1: Clone the QED Repositories

Clone the framework, then clone QED-Shared inside it. Both need to sit within the same root directory for the Gradle composite build to resolve correctly.

```shell
git clone https://github.com/AneVisser/qed-framework.git QEDFramework
cd QEDFramework
git clone https://github.com/AneVisser/QED-Shared.git
```

`QED-Shared` contains shared data classes that the framework and your SUT repositories depend on. When you add your own SUT repos later, clone them here as siblings of `QED-Shared`.

If GitHub asks for credentials, follow the prompts and enter your credentials.

---

## Step 2: Open the Framework in IntelliJ

Launch IntelliJ IDEA.

![img_3.png](img_3.png)

Select **Open** and choose the `QEDFramework` directory. Click **Trust Project** when prompted.

In `File → Settings → Appearance` you can choose a theme to your preference. In this documentation, the theme "Light" is chosen.

IntelliJ will detect the Gradle project and begin indexing. If it does not start automatically, go to the right-hand toolbar, click the **Gradle** button (1), and then the **Sync** button (2).

![img_4.png](img_4.png)

A successful sync results in a clean build with no errors:

![img_7.png](img_7.png)

---

## Step 3: Configure Java and Gradle

**Set the Project SDK:** Go to `File → Project Structure → Project` and set the SDK to Java 22.

![img_6.png](img_6.png)

**Gradle Settings:** Go to `File → Settings → Build, Execution, Deployment → Build Tools → Gradle` and confirm:

- Use Gradle from: **Gradle wrapper**
- Gradle JVM: **Java 22**

![img_5.png](img_5.png)

---

## Step 4: Add Gradle Projects for Test Suites

The framework comes with `qed-demos` — a set of demo test suites. To run them, import the demos module into the Gradle tool window:

- Open the Gradle tool window (right panel)
- Click the **+** button (Attach Gradle Project)
- Navigate to `qed-demos/build.gradle.kts` and select it
- IntelliJ will import it as a proper Gradle module alongside the framework

When you create your own SUT repo later, follow the same steps to import it.

---

## Step 5: Build the Framework

In the IntelliJ terminal (`View → Tool Windows → Terminal`):

```shell
gradlew clean build
```

---

## Step 6: Run a Demo Test

QED includes demo SUTs to help you validate your setup. The demos live inside the framework repository in the `qed-demos/` directory.

The demos project is a separate Gradle project that uses a composite build to depend on the framework. Open it as a separate IntelliJ project:

1. Go to `File → Open`
2. Browse to `QEDFramework\qed-demos\`
3. Choose **New Window** when prompted
4. Wait for Gradle sync to complete

Now set up a run configuration. In the top bar, click on **Current File** and select **Edit Configurations**.

![img_8.png](img_8.png)

Click the **+** in the top-left corner of the dialog and select **Gradle**.

![img_12.png](img_12.png)

Set the configuration as follows:

- **Run:** `clean test -Ptestsuite=uitestingplayground -Penvironment=dev`
- **Gradle project:** `C:\QEDFramework\qed-demos`

![img_9.png](img_9.png)

Click the **Run** button in the dialog, or click **OK** and launch from the top bar. The arrow button launches the suite; the bug button does the same but enables debugging.

![img_10.png](img_10.png)

The **Run** panel in IntelliJ will show the tests running. The first run is slower than usual as Playwright downloads Chromium browser binaries.

![img_13.png](img_13.png)

There will be one failure in the suite — this is intentional, to demonstrate how failures appear in the report.

![img_14.png](img_14.png)

If you see the browser launch and the tests execute, you're good to go. When the tests have finished, navigate to the report:

```
build/test-output/ExtentReport/TestExecutionReport.html
```

![img_11.png](img_11.png)

Right-click on it and select **Open In → Browser → \<Favourite Browser\>**. If you can view a report that looks like this, you have successfully run the demo suite:

![img_15.png](img_15.png)

---

## Available Demo Suites

Other demo suites can be run using the same configuration, changing the `-Ptestsuite` parameter:

| Suite | Run command |
|-------|-------------|
| UI Testing Playground | `clean test -Ptestsuite=uitestingplayground -Penvironment=dev` |
| API Challenges | `clean test -Ptestsuite=apichallenges -Penvironment=dev` |
| Mixed UI/API | `clean test -Ptestsuite=mixedUIAPI -Penvironment=dev` |