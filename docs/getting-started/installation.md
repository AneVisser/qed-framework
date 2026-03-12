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

### Step 4: Add gradle projects for test suites
The installer for the test framework comes with demos (qed-demos). In order to be able to run those tests, the gradle module within
that directory needs to be imported. Please follow these steps in IntelliJ:
- Open the Gradle tool window (right panel)
- Click the + button (Attach Gradle Project)
- Navigate to qed-demos/build.gradle.kts and select it
-  IntelliJ will import it as a proper Gradle module alongside qed-framework

If you develop your own suite for a specific app, you can follow the same steps to import the test suite.

## Step 5: Build the Framework

In IntelliJ (go to `View | Tool Windows | Terminal`):
```shell
gradlew clean build
```

## Step 6: Run a Demo Test

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


----
