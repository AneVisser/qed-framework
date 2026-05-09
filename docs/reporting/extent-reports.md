# ExtentReports Setup

[ExtentReports](https://extentreports.com/) is the reporting tool of choice for QED. It is a proven, detailed reporting tool with good configuration options, and is considered the most mature and feature-rich option available for test reporting in the Java/Kotlin ecosystem.

---

## Configuration

Reports for a system under test are configured in the associated config file:

```json
{
  "reporting": {
    "extent": {
      "theme": "DARK",
      "reportName": "mixed UI API report",
      "documentTitle": "UI API Report",
      "enableScreenshots": true
    }
  }
}
```

| Option | Values | Description |
|--------|--------|-------------|
| `theme` | `DARK`, `STANDARD` | Visual theme of the report |
| `reportName` | string | Displayed in the report header |
| `documentTitle` | string | Browser tab title |
| `enableScreenshots` | `true`, `false` | Captures screenshots on test failure |

---

## Report Location

After a test run, the report is generated at:

```
build/test-output/ExtentReport/TestExecutionReport.html
```

---

## Alternative Reporting Tools

QED supports the use of other reporting tools by installing their listeners — hooks for external reporters are available and the architecture is open to extension.

Allure Reports was evaluated as an alternative. While the listener approach worked, it proved technically incompatible with QED's reporting model: the default Allure listener could not be disabled when a custom one was installed, and it introduced a measurable slowdown in test execution. For these reasons it was not adopted, but the extension points remain available for teams who wish to integrate a different reporting tool.