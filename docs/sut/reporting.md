QED includes built-in support for [Extent Reports](https://extentreports.com/) as its default reporting
mechanism . 
Every test run generates a structured, interactive HTML report 
that summarizes execution results, metadata, and diagnostics.

<b>Setup</b>
Extent Reports is preconfigured in QED’s core. No additional setup is required for basic usage. Reports are automatically generated in the build/reports/qed directory.

<b>Report Contents</b>
Each report includes:

- Pass/fail status for each test
- Execution time and duration
- Test method names and descriptions
- Tags and categories (if configured)
- Exception stack traces for failures
- Screenshots or logs (optional, if enabled)

<b>Customization</b>

You can customize report behavior via QED’s configuration DSL:
```json
  "reporting" {
    "extent": {
      "theme": "DARK",
      "outputDir": "build/reports/qed",
      "enableScreenshots": true
    }
  }
```

<b>Default Behavior</b>

- A new report is generated for each test suite run.
- Reports are timestamped and stored locally.
- Extent is always enabled—no need to toggle it on.


