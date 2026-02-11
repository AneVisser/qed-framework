## These are the foundational building blocks of QED’s DSL:

Every test needs to be declared with a BaseTest property. This initialises everything
needed for TestNG and ExtentReports.

To define the type of test (UI, API or hybrid), you need to declare the following elements:

```kotlin
private val baseTest = BaseTest()
private val hasRest = HasRest(baseTest, urlKey = "apichallenges")
private val hasBrowser = HasBrowser(baseTest, urlKey = "uitestingurl")

class UI_RESTTest : TestContext(baseTest, hasBrowser, hasRest) {
    private class E2E_UITestingPlayground4Test : TestContext(baseTest, hasBrowser) {

        @Test(priority = 0, description = "example hybrid test", groups = ["All"])
        fun fourthTest() {
            // actual test code
            hasBrowser?.apply { ... }
            hasRest?.apply { ... }
        }
    }
}
```
The "hasBrowser" section gives access to all Browser (e.g. Playwright derived functions) functionality.
The "hasRest" section gives access to everything related to API testing (e.g. REST Assured derived functions)

## navigateToApp()
Launches the application under test. Typically used at the start of a UI or hybrid test 
within a hasBrowser?.apply block.

## startFromPage(\<page object>) { ... }
Declares the known starting point of a UI test. The lambda with receivers (the dots here)
provides access to all public properties from the page object that is accessed by StartFromPage.

Ensures the test begins in a predictable state.

## onPage(page object>) { ... }
Asserts that navigation succeeded. Same rules as StartFromPage applies.
Scopes subsequent actions to a specific page object.

## verify(\<description>) { ... }
Performs one or more expectations with a human-readable description.
Encourages descriptive, intention-revealing assertions.

## expect(...)
Fluent expectation builder (from "com.winterbe:expekt").
Supports equality, containment, null checks, and more.

## rest.send(...)
Sends RESTful requests using idiomatic Kotlin. Supports all HTTP verbs, status code validation, and JSON payloads.