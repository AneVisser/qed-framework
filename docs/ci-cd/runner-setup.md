# CI/CD Overview & Runner Setup

QED test suites are designed to run on a **self-hosted GitHub Actions runner**. This page covers why that is required, how to set one up, and how to configure it for both API and UI test execution.

---

## Why a Self-Hosted Runner?

GitHub-hosted runners are ephemeral — they are wiped after every job. This makes them unsuitable for QED for two reasons:

- **Playwright system libraries** — Chromium requires a set of native Linux libraries that are not present on GitHub-hosted Ubuntu runners by default. Installing them on every run adds significant overhead.
- **Persistent cache** — Gradle dependencies and Playwright browser binaries are large. On a self-hosted runner they persist between runs naturally, without any cache upload/download overhead.

A self-hosted runner on a stable VM is the recommended approach.

---

## Runner Installation

These instructions assume Ubuntu 24.04 LTS. The runner should be installed as a service so it starts automatically on reboot.

**Step 1: Create a runner user**

It is good practice to run the GitHub Actions runner as a dedicated non-root user:

```bash
sudo useradd -m -s /bin/bash runner
sudo usermod -aG sudo runner
```

**Step 2: Download and configure the runner**

In GitHub, go to your repository → **Settings → Actions → Runners → New self-hosted runner**. Select Linux and follow the generated instructions. They will look similar to:

```bash
mkdir actions-runner && cd actions-runner
curl -o actions-runner-linux-x64.tar.gz -L https://github.com/actions/runner/releases/download/v2.x.x/actions-runner-linux-x64-2.x.x.tar.gz
tar xzf ./actions-runner-linux-x64.tar.gz
./config.sh --url https://github.com/<your-org>/<your-repo> --token <generated-token>
```

**Step 3: Install as a service**

```bash
sudo ./svc.sh install
sudo ./svc.sh start
```

Verify it is running:

```bash
sudo ./svc.sh status
```

The runner should appear as **Idle** in GitHub under Settings → Actions → Runners.

---

## Java Setup

QED requires Java 22. Install Temurin via SDKMAN or directly:

```bash
sudo apt install -y wget apt-transport-https
wget -qO - https://packages.adoptium.net/artifactory/api/gpg/key/public | sudo tee /etc/apt/trusted.gpg.d/adoptium.asc
echo "deb https://packages.adoptium.net/artifactory/deb $(lsb_release -cs) main" | sudo tee /etc/apt/sources.list.d/adoptium.list
sudo apt update
sudo apt install -y temurin-22-jdk
java -version
```

---

## Playwright System Libraries

UI tests use Playwright to drive Chromium. On Ubuntu 24.04, a set of native system libraries must be installed for Chromium to launch. Without these, the browser will fail to start with a missing library error.

Install the required libraries:

```bash
sudo apt install -y \
  libatk1.0-0 \
  libatk-bridge2.0-0 \
  libcups2 \
  libdrm2 \
  libxkbcommon0 \
  libxcomposite1 \
  libxdamage1 \
  libxfixes3 \
  libxrandr2 \
  libgbm1 \
  libpango-1.0-0 \
  libcairo2 \
  libasound2t64 \
  libnspr4 \
  libnss3 \
  libx11-xcb1 \
  libxcb-dri3-0
```

These only need to be installed once on the runner VM. They persist between runs.

---

## Playwright Browser Binaries

On the first run, Playwright will download Chromium binaries to `~/.cache/ms-playwright`. This happens automatically — no manual step is needed. The binaries persist on the self-hosted runner between runs, so subsequent runs are fast.

> Do not add a Playwright cache step to your workflow. On a self-hosted runner, caching adds upload/download overhead with no benefit — the binaries already persist on disk.

The same applies to Gradle dependencies — do not add a Gradle cache step to workflows running on a self-hosted runner.

---

## SSH Key for Private Repositories

QED test suites typically depend on private shared repositories (e.g. `QED-Shared`, `QED-Shared-<app>`). The runner checks these out during the workflow using an SSH deploy key.

**Step 1: Generate a key pair on the runner**

```bash
ssh-keygen -t ed25519 -C "github-actions-runner" -f ~/.ssh/id_ed25519 -N ""
```

**Step 2: Add the public key as a deploy key**

Copy the public key:

```bash
cat ~/.ssh/id_ed25519.pub
```

Add it to each private repository that the runner needs to access: **GitHub repo → Settings → Deploy keys → Add deploy key**. Enable "Allow read access".

**Step 3: Add the private key as a repository secret**

Use the GitHub CLI to avoid copy-paste issues with multiline keys:

```bash
gh secret set RUNNER_SSH_PRIVATE_KEY --repo <your-org>/<your-repo> < ~/.ssh/id_ed25519
```

This secret must be present in every repository whose workflow calls a QED test workflow via `workflow_call`, since `secrets: inherit` only passes down secrets that exist in the calling repository.

---

## Secrets Configuration

The following secrets are required in any repository that calls a QED UI test workflow. Add them under **Settings → Secrets and variables → Actions**.

| Secret | Purpose |
|--------|---------|
| `RUNNER_SSH_PRIVATE_KEY` | SSH key for checking out private QED repos |
| `QED_PASSWORD_ADMIN` | Password for the admin test user |
| `QED_PASSWORD_MANAGER` | Password for the manager test user |
| `QED_PASSWORD_USER` | Password for the standard test user |
| `QED_PASSWORD_PARENT` | Password for the parent test user |

Add additional user secrets as needed to match the test users defined in your `QED-Shared-<app>` module.

For API-only test workflows, only `RUNNER_SSH_PRIVATE_KEY` is required unless your tests also use authenticated endpoints.

---

## Environment Variables

QED uses the following environment variables at runtime:

| Variable | Purpose | CI value | Local value |
|----------|---------|----------|-------------|
| `QED_HEADLESS` | Controls browser visibility | `true` | unset (headed) |
| `QED_PASSWORD_*` | Test user credentials | from secrets | from local env or config |

`QED_HEADLESS` defaults to headed mode when unset, so local runs open a real browser window. Setting it to `true` in the workflow enables headless mode on CI without any code changes.

---

## Environment Key Convention

QED uses short environment keys passed as workflow inputs. The convention used across all workflows is:

| Environment    | Key       | Also known as |
|----------------|-----------|---------------|
| Development    | `dev`     |               |
| Staging        | `stag`    | staging       |
| Pre-production | `preprod` |               |
| Production     | `prod`    |               |

These keys map to entries in your QED project's environment configuration file and must be consistent across your test suite and workflow definitions.