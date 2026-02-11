## First Test Run: UITestingPlayground

QED includes a demo test suite targeting [UITestingPlayground](https://uitestingplayground.com/) — a sandbox site designed for UI automation practice. This guide walks you through running your first test and viewing the generated report.

---

## Step 1: Open the Demo Test

Navigate to:

src/test/kotlin/demo/UITestingPlaygroundTest.kt

This test exercises basic UI interactions using QED’s DSL.

---

## Step 2: Configure IntelliJ to Run the Test

To run the test smoothly, set up a Gradle run configuration:

1. Go to `Run → Edit Configurations`
2. Click the `+` icon → Select **Gradle**
3. Name it: `Run UITestingPlayground Test`
4. Set:
   - **Tasks**: `test`
   - **Working directory**: Project root
   - **VM options**: `-Denv=uitestingplayground` _(or whatever environment key your test expects)_
   - **Test filter (optional)**: `--tests *UITestingPlaygroundTest`

Click **OK** to save.

---

## Step 3: Run the Test

Use the newly created configuration to run the test:

- Select `Run UITestingPlayground Test` from the dropdown
- Click the green play button

You should see the browser launch and interact with the playground site.

---

## Step 4: View the Extent Report

After the test completes, QED generates a rich HTML report:

build/test-output/ExtentReport/TestExecutionReport.html


Open this file in your browser to view:

- Test steps and outcomes
- Screenshots (if configured)
- Execution time and metadata

> _Tip: Bookmark the report file for quick access during development._

---


---

