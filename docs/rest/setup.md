# REST Setup

QED supports expressive, minimal API testing through its REST DSL.
This page outlines how to configure REST support in your test suite, including environment setup,
endpoint definitions, and idiomatic wiring.

---

## Core Modules

To enable REST testing, you'll need:

- A base test context (typically `BaseTest`)
- A REST wrapper (`HasRest`) that injects endpoint configuration

```kotlin
private val baseTest = BaseTest()
private val hasRest = HasRest(baseTest, urlKey = "url")
```

---

## Environment Configuration

```json
"environments": {
  "dev": {
    "url": "https://apichallenges.eviltester.com"
  },
  "test": {
    "url": "ENV_MYAPP"
  }
}
```

**Secure URLs**

If your endpoint is sensitive or dynamic, use an environment variable reference:

```json
"url": "ENV_MYAPP"
```

Then set the corresponding environment variable on the host:

```bash
MYAPP=http://localhost/myapp
```

---

## Endpoint Definitions

QED requires idiomatic endpoint declarations using enums or sealed classes:

```kotlin
enum class APIChalURLPath(val path: String) {
    SIM_ENTITIES("/sim/entities"),
    TODO_LIST("/todos")
}
```

For full type safety — including payload and response type checking — see [Sharing with Ktor](sharing-with-ktor.md).

---

## Shared Directory

QED allows applications (e.g., Ktor services) to reuse tested URL definitions
from the test framework. To enable this, a sibling directory to your
QED project must be created for the shared interface and URL definitions.
By default, this directory is called `QED-Shared`.

**Directory structure:**

```
Projects
├── QED             # QED test framework
├── QED-Shared      # Shared interfaces and URL definitions
```

**`QED-Shared/settings.gradle.kts`:**

```kotlin
rootProject.name = "QED-Shared"
```

**`QED-Shared/build.gradle.kts`:**

```kotlin
plugins {
    kotlin("jvm") version "2.0.20"
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))
}

group = "com.example"
version = "1.0.0"
```

**`QED-Shared/src/main/kotlin/IURLPath.kt`:**

```kotlin
package qed.testbaseclass

import kotlin.reflect.KClass

interface IURLPath {
    val route: String
    val method: RequestType
    val responseKind: PayloadKind?
    val payloadKind: PayloadKind?
}

sealed class PayloadKind {
    data class Single(val type: KClass<*>) : PayloadKind()
    data class ListOf(val type: KClass<*>) : PayloadKind()
}

enum class RequestType {
    GET,
    POST,
    PUT,
    DELETE,
    PATCH
}
```

**`QED/settings.gradle.kts`** — include the shared build:

```kotlin
rootProject.name = "QED"

includeBuild("../QED-Shared")
```

**`QED/build.gradle.kts`** — declare the dependency:

```kotlin
plugins {
    kotlin("jvm") version "2.0.20"
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation("com.example:qed-shared:1.0.0")
}
```

---

## Application-Specific URL Paths

For URL path definitions that also need to be shared with a Ktor application, create
a `sut` subdirectory inside `QED-Shared`, with a subdirectory specific to the application:

```
Projects
├── QED
├── QED-Shared
│   └── sut
│       └── <application>
```

Define the URL paths there according to the following example:

```kotlin
enum class APIChalURLPath(
    private val path: String,
    override val method: RequestType,
    override val responseKind: PayloadKind?,
    override val payloadKind: PayloadKind?,
) : IURLPath {
    POST_SIM_ENTITIES("/sim/entities", RequestType.POST, Single(Resp_SimEntities::class), Single(SimEntities::class)),
    GET_SIM_ENTITIES("/sim/entities/{entity}", RequestType.GET, null, null),
    PUT_SIM_ENTITIES("/sim/entities/{entity}", RequestType.PUT, null, Single(SimEntities::class)),
    TODOS("/todos", RequestType.GET, null, null);

    override val route: String
        get() = this.path
}
```

For both payloads and responses, use a data class when the types are known. Leave these fields `null`
for untyped requests or responses. Use `Single` for objects and `ListOf` for arrays.

Route paths are plain strings, with placeholders for parameters in curly brackets. QED supports
embedded parameters, for example `/api/calculate/{farmId}/pasture`.

See also [Sharing with Ktor](sharing-with-ktor.md).

---

## Build Instructions

1. Open the `QED-Shared` project and build it in Gradle so it is available for composite build.
2. Open the `QED` project and sync Gradle — the shared interface will now be available in the test framework.

This setup ensures that only tested endpoints are used by the application, and that the test framework
acts as a verified contract layer between your Ktor app and the REST endpoints.

---

## Optional Configuration

> Header injection and per-request auth token support are under development and will be documented here in a future release.