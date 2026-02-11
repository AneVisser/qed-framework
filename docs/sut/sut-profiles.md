QED supports flexible configuration of your System Under Test (SUT) through a simple JSON file. 
This file defines environments, browser settings, reporting preferences, 
and metadata for performance tracking.

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

## Environment Configuration

Each environment (e.g. dev, test, prod) defines a base URL for the SUT.

If your URL is sensitive or dynamic, you can reference it via environment variables:

```json
"url": "ENV_MYAPP"
```
This tells QED to resolve MYAPP from the system environment:
ENV_MYAPP = http://localhost/myapp

- Keeps secrets out of source control
- Supports local overrides and CI/CD pipelines

## Browser Configuration
```json
"browser": "chromium"
```
Supported values: "chromium", "firefox", "webkit" — depending on your Playwright setup.

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

- theme: "STANDARD" or "DARK"
- enableScreenshots: captures UI state on failure
- reportName and documentTitle: customize branding

## Test Run Metadata

```json
"testrunmetadata": {
  "repository": "C:\\Projects\\TestingFramework",
  "sut": "apichallenges",
  "maxRunsPerCommit": 5,
  "maxCommitsToKeep": 20
}
```
- repository: path the Git repo of your system under test
- sut: name of the system under test (used for perf-history subdirectory)
- maxRunsPerCommit: how many runs to retain per commit
- maxCommitsToKeep: how many commits to retain in history

## Tips & Best Practices

- Use ENV_ for secret url's and local overrides
- Keep environment names consistent across teams
- Use descriptive sut names for clear telemetry separation
- Validate your config early — malformed JSON will prevent test execution
- Consider versioning your config file alongside your test suite