# SUT Profiles

QED supports flexible configuration of your System Under Test (SUT) through a simple JSON file. This file defines environments, browser settings, reporting preferences, and metadata for performance tracking.

```json
{
  "environments": {
    "dev": {
      "url": "https://apichallenges.eviltester.com"
    },
    "test": {
      "url": "https://apichallenges.eviltester.com"
    }
  },
  "browser": "chromium",
  "reporting": {
    "extent": {
      "theme": "STANDARD",
      "reportName": "API Challenges",
      "documentTitle": "API Challenges Report",
      "enableScreenshots": true
    }
  },
  "testrunmetadata": {
    "repository": "C:\\Projects\\TestingFramework",
    "sut": "apichallenges",
    "maxRunsPerCommit": 5,
    "maxCommitsToKeep": 20
  }
}
```

---

## Environment Configuration

Each environment key (e.g. `dev`, `test`, `preprod`) defines a base URL for the SUT:

```json
"environments": {
  "dev": {
    "url": "https://apichallenges.eviltester.com"
  }
}
```

**Secure URLs**

If your URL is sensitive or dynamic, use the `ENV_` prefix to reference a system environment variable:

```json
"url": "ENV_MYAPP"
```

QED will resolve this by looking up the environment variable `MYAPP` at runtime:

```
MYAPP=http://localhost/myapp
```

- Keeps secrets out of source control
- Supports local overrides and CI/CD pipelines

---

## Browser Configuration

```json
"browser": "chromium"
```

Supported values are `chromium`, `firefox`, and `webkit`, depending on your Playwright setup.

---

## Reporting Configuration

```json
"reporting": {
  "extent": {
    "theme": "STANDARD",
    "reportName": "API Challenges",
    "documentTitle": "API Challenges Report",
    "enableScreenshots": true
  }
}
```

| Option | Values | Description |
|--------|--------|-------------|
| `theme` | `DARK`, `STANDARD` | Visual theme of the report |
| `reportName` | string | Displayed in the report header |
| `documentTitle` | string | Browser tab title |
| `enableScreenshots` | `true`, `false` | Captures screenshots on test failure |

See [ExtentReports Setup](../reporting/extentreports-setup.md) for full reporting documentation.

---

## Test Run Metadata

```json
"testrunmetadata": {
  "repository": "C:\\Projects\\TestingFramework",
  "sut": "apichallenges",
  "maxRunsPerCommit": 5,
  "maxCommitsToKeep": 20
}
```

| Field | Description |
|-------|-------------|
| `repository` | Path to the Git repo of the system under test — used to retrieve the latest commit number |
| `sut` | Name of the system under test — used as the subdirectory name under `perf-history/` |
| `maxRunsPerCommit` | Number of runs to retain per commit |
| `maxCommitsToKeep` | Number of commits to retain in performance history |

See [Performance Testing](../rest/performance-testing.md) for full performance history documentation.

---

## Tips

- Use the `ENV_` prefix for sensitive URLs and local overrides
- Keep environment key names consistent across all config files and workflow inputs
- Use descriptive `sut` names for clear separation in performance history
- Validate your config early — malformed JSON will prevent test execution
- Version your config file alongside your test suite