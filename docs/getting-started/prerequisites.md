# Prerequisites

Before you dive into QED, make sure your system meets the minimum requirements. QED is designed to run on modest hardware, but a modern setup will improve performance and developer experience.

---

## System Requirements

| Component  | Recommended                               | Minimum                                    |
|------------|-------------------------------------------|--------------------------------------------|
| OS         | Windows 11 / macOS Ventura / Ubuntu 22.04 | Windows 10 / macOS Monterey / Ubuntu 20.04 |
| RAM        | 16 GB                                     | 8 GB                                       |
| CPU        | Quad-core (Intel i5 / Ryzen 5 or better)  | Dual-core                                  |
| Disk Space | 2–4 GB for repo, dependencies, reports    | 1 GB                                       |
| Display    | Full HD (1920×1080) or higher             | 1366×768                                   |

> QED tests may launch browsers and run concurrent threads — more RAM and CPU cores improve performance.

---

## Software Requirements

| Tool              | Version / Notes                               |
|-------------------|-----------------------------------------------|
| **Java JDK**      | 22 or higher                                  |
| **IntelliJ IDEA** | Community Edition or Ultimate                 |
| **Gradle**        | Wrapper included — no separate install needed |
| **Git**           | Latest stable version                         |

---

## Platform Notes

### Windows
Tested on Windows 10 and Windows 11. Ensure `JAVA_HOME` is set correctly in system environment variables and that the `bin` directory is on your `PATH`.

### macOS
Works well with Homebrew-installed Java and Git. IntelliJ auto-detects the Gradle wrapper.

### Linux (Ubuntu/Debian)
Install Java via `apt` or SDKMAN. Playwright requires a set of native system libraries — see [CI/CD Runner Setup](../ci-cd/runner-setup.md) for the full list.

> On Linux you may need to grant execution permissions to the Gradle wrapper: `chmod +x gradlew`

---

## Verifying Your Setup

Once the tools are installed, confirm everything is on your `PATH`:

```shell
java -version
git --version
```