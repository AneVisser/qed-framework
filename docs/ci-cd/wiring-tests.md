# Wiring Tests into a Deploy Pipeline

This page covers how to structure QED test workflows and integrate them into a GitHub Actions deploy pipeline. It assumes the runner is already set up as described in [CI/CD Overview & Runner Setup](cicd-runner-setup.md).

---

## Design Principles

- **API tests** gate the backend deploy — they run after the backend is deployed and block promotion if they fail.
- **UI tests** gate the frontend deploy — they run after the frontend is deployed, by which point the backend is already up.
- Both test workflows are defined as **reusable workflows** (`workflow_call`) so they can be called from any deploy pipeline, and also triggered manually (`workflow_dispatch`) for standalone runs.

---

## API Test Workflow

Create `.github/workflows/api-tests.yml` in your API test repository (e.g. `qed-sut-<app>-rest`):

```yaml
name: API Tests

on:
  workflow_call:
    inputs:
      environment:
        description: 'QED environment name (stag or preprod)'
        required: true
        type: string

  workflow_dispatch:
    inputs:
      environment:
        description: 'Target environment'
        required: true
        default: 'stag'
        type: choice
        options:
          - stag
          - preprod

jobs:
  api-tests:
    name: API Tests (${{ inputs.environment }})
    runs-on: self-hosted

    steps:
      - name: Checkout QED-Shared
        uses: actions/checkout@v4
        with:
          repository: <your-org>/QED-Shared
          path: qed-deps/QED-Shared
          ssh-key: ${{ secrets.RUNNER_SSH_PRIVATE_KEY }}

      - name: Checkout QED-Shared-<app>
        uses: actions/checkout@v4
        with:
          repository: <your-org>/QED-Shared-<app>
          path: qed-deps/QED-Shared-<app>
          ssh-key: ${{ secrets.RUNNER_SSH_PRIVATE_KEY }}

      - name: Checkout qed-sut-<app>-rest
        uses: actions/checkout@v4
        with:
          repository: <your-org>/qed-sut-<app>-rest
          path: qed-deps/qed-sut-<app>-rest
          ssh-key: ${{ secrets.RUNNER_SSH_PRIVATE_KEY }}

      - name: Set up Java 22
        uses: actions/setup-java@v4
        with:
          distribution: temurin
          java-version: '22'

      - name: Run API tests
        env:
          GRADLE_OPTS: "-Xmx1024m -Dorg.gradle.daemon=false"
          QED_PASSWORD_ADMIN:   ${{ secrets.QED_PASSWORD_ADMIN }}
          QED_PASSWORD_MANAGER: ${{ secrets.QED_PASSWORD_MANAGER }}
          QED_PASSWORD_USER:    ${{ secrets.QED_PASSWORD_USER }}
        run: |
          chmod +x ./gradlew
          ./gradlew clean test -Ptestsuite=<app> -Penvironment=${{ inputs.environment }}

      - name: Upload test report
        if: always()
        uses: actions/upload-artifact@v4
        with:
          name: api-test-report-${{ inputs.environment }}
          path: |
            qed-deps/qed-sut-<app>-rest/build/reports/tests/test/
            qed-deps/qed-sut-<app>-rest/build/test-output/ExtentReport/
          retention-days: 2
```

---

## UI Test Workflow

Create `.github/workflows/ui-tests.yml` in your UI test repository (e.g. `qed-sut-<app>-ui`):

```yaml
name: UI Tests

on:
  workflow_call:
    inputs:
      environment:
        description: 'QED environment name (stag or preprod)'
        required: true
        type: string

  workflow_dispatch:
    inputs:
      environment:
        description: 'Target environment'
        required: true
        default: 'stag'
        type: choice
        options:
          - stag
          - preprod

jobs:
  ui-tests:
    name: UI Tests (${{ inputs.environment }})
    runs-on: self-hosted
    defaults:
      run:
        working-directory: qed-framework/qed-sut-<app>-ui

    steps:
      - name: Checkout qed-framework
        uses: actions/checkout@v4
        with:
          repository: <your-org>/qed-framework
          path: qed-framework
          ssh-key: ${{ secrets.RUNNER_SSH_PRIVATE_KEY }}

      - name: Checkout QED-Shared
        uses: actions/checkout@v4
        with:
          repository: <your-org>/QED-Shared
          path: qed-framework/QED-Shared
          ssh-key: ${{ secrets.RUNNER_SSH_PRIVATE_KEY }}

      - name: Checkout QED-Shared-<app>
        uses: actions/checkout@v4
        with:
          repository: <your-org>/QED-Shared-<app>
          path: qed-framework/QED-Shared-<app>
          ssh-key: ${{ secrets.RUNNER_SSH_PRIVATE_KEY }}

      - name: Checkout qed-sut-<app>-ui
        uses: actions/checkout@v4
        with:
          repository: <your-org>/qed-sut-<app>-ui
          path: qed-framework/qed-sut-<app>-ui
          ssh-key: ${{ secrets.RUNNER_SSH_PRIVATE_KEY }}

      - name: Set up Java 22
        uses: actions/setup-java@v4
        with:
          distribution: temurin
          java-version: '22'

      - name: Run UI tests
        env:
          GRADLE_OPTS: "-Xmx1024m -Dorg.gradle.daemon=false"
          QED_HEADLESS: "true"
          QED_PASSWORD_ADMIN:   ${{ secrets.QED_PASSWORD_ADMIN }}
          QED_PASSWORD_MANAGER: ${{ secrets.QED_PASSWORD_MANAGER }}
          QED_PASSWORD_USER:    ${{ secrets.QED_PASSWORD_USER }}
        run: |
          chmod +x ./gradlew
          ./gradlew clean test -Ptestsuite=<app> -Penvironment=${{ inputs.environment }}

      - name: Upload test report
        if: always()
        uses: actions/upload-artifact@v4
        with:
          name: ui-test-report-${{ inputs.environment }}
          path: |
            qed-framework/qed-sut-<app>-ui/build/reports/tests/test/
            qed-framework/qed-sut-<app>-ui/build/test-output/ExtentReport/
          retention-days: 2
```

> Note: `QED_HEADLESS: "true"` enables headless browser mode on CI. Omit this variable when running locally to get a headed browser for debugging.

---

## Wiring into the Backend Deploy Pipeline

In your backend deploy workflow, call the API test workflow after each environment deploy. The `needs:` dependency ensures tests only run once the backend is up, and a failure blocks promotion.

```yaml
  # API Tests — Staging
  api-tests-staging:
    name: API Tests - Staging
    needs: deploy-staging
    uses: <your-org>/qed-sut-<app>-rest/.github/workflows/api-tests.yml@master
    with:
      environment: stag
    secrets: inherit

  # API Tests — Pre-prod
  api-tests-preprod:
    name: API Tests - Pre-prod
    needs: deploy-preprod
    uses: <your-org>/qed-sut-<app>-rest/.github/workflows/api-tests.yml@master
    with:
      environment: preprod
    secrets: inherit
```

---

## Wiring into the Frontend Deploy Pipeline

UI tests belong in the frontend deploy pipeline — by the time the frontend has deployed, the backend is already up. Wire them in after each environment deploy:

```yaml
  # UI Tests — Staging
  ui-tests-staging:
    name: UI Tests - Staging
    needs: deploy-staging
    uses: <your-org>/qed-sut-<app>-ui/.github/workflows/ui-tests.yml@master
    with:
      environment: stag
    secrets: inherit

  # UI Tests — Pre-prod
  ui-tests-preprod:
    name: UI Tests - Pre-prod
    needs: deploy-preprod
    uses: <your-org>/qed-sut-<app>-ui/.github/workflows/ui-tests.yml@master
    with:
      environment: preprod
    secrets: inherit
```

---

## Secrets: `secrets: inherit`

The `secrets: inherit` directive passes all secrets from the calling repository down to the called workflow. This means every secret referenced in `api-tests.yml` or `ui-tests.yml` (including `RUNNER_SSH_PRIVATE_KEY` and all `QED_PASSWORD_*` secrets) must be defined in the **calling** repository — not just in the test repo.

---

## Full Pipeline Flow

The recommended end-to-end promotion flow is:

```
Push to main
  └── Build backend
        └── Deploy backend (staging)
              └── API tests (staging)         ← blocks if failing
                    └── [manual] Deploy backend (preprod)
                          └── API tests (preprod)    ← blocks if failing

Push to main
  └── Build frontend
        └── Deploy frontend (staging)
              └── UI tests (staging)          ← blocks if failing
                    └── [manual] Deploy frontend (preprod)
                          └── UI tests (preprod)     ← blocks if failing
```

Backend and frontend pipelines run independently. The implicit ordering is maintained by your promotion workflow — the backend must be deployed and API tests must pass before you manually promote to preprod, at which point the frontend deploy and UI tests follow.