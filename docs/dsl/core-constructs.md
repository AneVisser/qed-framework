# Core Constructs

These are the foundational building blocks of QED's DSL. Every test starts with a `BaseTest` declaration, which initialises everything needed for TestNG and ExtentReports.

---

## Test Context Setup

To define the type of test — UI, API, or hybrid — declare the following elements:

```kotlin
private val baseTest = BaseTest()
private val hasRest = HasRest(baseTest, urlKey = "apichallenges")
private val hasBrowser = HasBrowser(baseTest, urlKey = "uitestingurl")

class MyTest : TestContext(baseTest, hasBrowser, hasRest) {

    @Test(priority = 0, description = "example hybrid test", groups = ["All"])
    fun myHybridTest() {
        hasBrowser?.apply {
            // browser interactions here
        }
        hasRest?.apply {
            // API interactions here
        }
    }
}
```

- `hasBrowser` — provides access to all browser (Playwright-derived) functionality
- `hasRest` — provides access to all API testing (REST-derived) functionality
- Omit either one for a pure UI or pure API test

---

## `navigateToApp()`

Launches the application under test. Used at the start of a UI or hybrid test inside a `hasBrowser?.apply` block.

---

## `startFromPage(<pageObject>) { ... }`

Declares the known starting point of a UI test. The lambda with receiver gives access to all public properties and functions of the page object. Ensures the test begins in a predictable state.

---

## `onPage(<pageObject>) { ... }`

Asserts that navigation to a page succeeded, then scopes all subsequent actions to that page object. The same receiver rules apply as for `startFromPage`.

---

## `verify(<description>) { ... }`

Performs one or more expectations grouped under a human-readable description. Encourages descriptive, intention-revealing assertions that are visible in the test report.

---

## `expect(...)`

Fluent expectation builder (from `com.winterbe:expekt`). Supports equality, containment, null checks, and more:

```kotlin
verify("user should have admin role") {
    expect(user.name).to.equal("Alice")
    expect(user.roles).to.contain("admin")
}
```

---

## `rest.send(...)`

Sends RESTful requests using idiomatic Kotlin. Supports all HTTP verbs, status code validation, and JSON payloads. See [DSL for APIs](../rest/dsl-for-apis.md) for full usage.