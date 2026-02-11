## REST Setup
QED supports expressive, minimal API testing through its REST DSL. 
This page outlines how to configure REST support in your test suite, including environment setup, 
endpoint definitions, and idiomatic wiring.

## Core Modules
To enable REST testing, you’ll need:

- A base test context (typically BaseTest)
- A REST wrapper (HasRest) that injects endpoint configuration

**Example Setup**:
```kotlin
private val baseTest = BaseTest()
private val hasRest = HasRest(baseTest, urlKey = "url")
```

**Environment Configuration**:
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

**Secure URLs**:

If your endpoint is sensitive or dynamic, use environment variables:
```json
    "url": "ENV_MYAPP"
```
and set the environment variable for ENV_MYAPP:
MYAPP=http://localhost/myapp

QED requires idiomatic endpoint declarations using enums or sealed classes:
```kotlin
enum class APIChalURLPath(val path: String) {
    SIM_ENTITIES("/sim/entities"),
    TODO_LIST("/todos")
}
```
**Optional Configuration**:
```kotlin
enum class APIChalURLPath(val path: String) {
    SIM_ENTITIES("/sim/entities"),
    TODO_LIST("/todos")
}
```

**IMPORTANT!** 

**Shared directory**:
QED allows applications (e.g., Ktor services) to reuse tested URL definitions 
from the test framework. To enable this, a sibling directory to your 
QED project must be created for the shared interface and URL definitions. 
By default, this directory is called QED-Shared. '**QED-Shared**'.
Directory structure example

Projects<br>
├─ QED &nbsp;&nbsp;               # QED test framework<br>
├─ QED-Shared &nbsp;&nbsp;        # Shared interfaces and URL definitions<br>


settings.gradle.kts
```kotlin
rootProject.name = "QED-Shared"
```

build.gradle.kts
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

// make it publishable to the composite build
group = "com.example"
version = "1.0.0"
```

and in the subdirectory /src/main/kotlin/IURLPath.kt:
```kotlin
package qed.testbaseclass

import kotlin.reflect.KClass

interface IURLPath {
    val route : String
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
Then, the file settings.gradle.kts in your QED root directory should contain th efillowing sections: 
```kotlin
rootProject.name = "QED"

includeBuild("../QED-Shared")
```

and build.gradle.kts:
```kotlin
plugins {
    kotlin("jvm") version "2.0.20"
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))

    // reference the shared build via the group/artifactId
    implementation("com.example:qed-shared:1.0.0")
}
```
## Build Instruction
- Open the QED-Shared project and build it in Gradle (so it’s available for composite build).

- Open the QED project and sync Gradle — the shared interface will now be available in the test framework.

This setup allows the test framework and any application (e.g., Ktor) 
to reuse URL definitions, ensuring that only tested endpoints are used.
For the actual ULRPath definitions that need to be shared with a kTor application, create 
a subdirectory 'sut' in 'EQD-Shared', and below that a subdirectory specific to the 
application:
Projects<br>
├─ QED                # QED test framework  <br>
├─ QED-Shared         # Shared interfaces and URL definitions<br>
&nbsp;&nbsp;&nbsp;&nbsp;   ├─sut  <br>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;  ├─ &lt;application&gt;  <br>

and define the URLPaths in thet directory according to the following example:
```kotlin

enum class APIChalURLPath(private val path: String,
                          override val method: RequestType,
                          override val responseKind:PayloadKind?,
                          override val payloadKind: PayloadKind?,
    )
    : IURLPath {
    POST_SIM_ENTITIES("/sim/entities", RequestType.POST, Single(Resp_SimEntities::class), Single(SimEntities::class)),
    GET_SIM_ENTITIES("/sim/entities/{entity}", RequestType.GET, null, null),
    PUT_SIM_ENTITIES("/sim/entities/{entity}", RequestType.PUT, null, Single(SimEntities::class)),
    TODOS("/todos", RequestType.GET, null, null)
    ;

    override val route: String
        get() = this.path

```
Please note that for both the payloads and the responses a data class can be used if you know the types. 
If not, you can leave these fields null (for get requests and responses respectively).
For Objects, you needs to use the 'Single' cast, for arrays use the 'ListOf' cast.

The route path is described by a string, and a placeholder for parameters is placed in curly brackets. QED supports embedded parameters,
for example **/api/calculate/{farmId}/pasture**

See also the page about 'Sharing with kTor'

---
Coming soon:

**Optional Configuration**
```kotlin
val hasRest = HasRest(baseTest, urlKey = "url").apply {
    setAuthToken("Bearer abc123")
    setDefaultHeaders(mapOf("Accept" to "application/json"))
}
```
or inject headers per request:
```kotlin
val result = rest.sendUntyped(
    endpoint = APIChalURLPath.SIM_ENTITIES,
    headers = mapOf("Authorization" to "Bearer abc123"),
    expectedStatus = 200
)
```


