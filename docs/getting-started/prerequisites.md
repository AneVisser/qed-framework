# Prerequisites

Before you dive into QED, make sure your system meets the minimum requirements. QED is designed to run on modest hardware, but a modern setup will improve performance and developer experience.

---

## System Requirements

| Component     | Recommended                                         | Minimum Viable                     |
|--------------|-----------------------------------------------------|------------------------------------|
| OS           | Windows 11 / macOS Ventura / Ubuntu 22.04           | Windows 10 / macOS Monterey / Ubuntu 20.04 |
| RAM          | 16 GB                                               | 8 GB                               |
| CPU          | Quad-core (Intel i5/Ryzen 5 or better)              | Dual-core                          |
| Disk Space   | 2–4 GB for repo, dependencies, reports              | 1 GB                               |
| Display      | Full HD (1920×1080) or higher (dual setup is ideal) | 1366×768                           |

> _Note: QED tests may launch browsers and run concurrent threads — more RAM and CPU cores help._

---

## Software Requirements

| Tool             | Version / Notes                      |
|------------------|--------------------------------------|
| **Java JDK**     | 17 or higher (LTS recommended)       |
| **IntelliJ IDEA**| Community Edition or Ultimate        |
| **Gradle**       | Wrapper included — no install needed |
| **Git**          | Latest stable version                |
| **Chrome/Firefox** | For browser-based UI testing       |

---

## Platform Notes

### Windows
- Tested on Windows 11 and native setup.
- Ensure environment variables (`JAVA_HOME`, `PATH`) are correctly set.

### macOS
- Works well with Homebrew-installed Java and Git.
- IntelliJ auto-detects Gradle wrapper.

### Linux (Ubuntu/Debian)
- Install Java via `apt` or SDKMAN.
- Chrome/Firefox may need additional dependencies (e.g. `libnss3`, `chromedriver`).

> _You may need to grant execution permissions to Gradle wrapper: `chmod +x gradlew`_

---

## Optional Tools

- **Docker**: For isolated SUT environments (future support)
- **MkDocs**: If contributing to QED documentation

---

## Verifying Setup

Once installed, run:

```shell
java -version
git --version
./gradlew --version
```
