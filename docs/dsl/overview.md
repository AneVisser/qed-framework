## QED DSL Overview

QED’s Domain-Specific Language (DSL) is designed to make test automation expressive, readable, and maintainable. Whether you're testing a UI, an API, or a hybrid flow, the DSL reflects the core actions testers actually perform — without boilerplate or ceremony.

---

## Supported Test Types

QED currently supports three test modes:

- **UI Tests**: Browser-based interactions using Playwright
- **API Tests**: RESTful calls and validations using REST-assured and Moshi
- **Hybrid Tests**: Seamless combination of UI and API steps

---

## DSL Philosophy

In a typical UI test, the tester:

1. **Navigates** to the application
2. **Executes actions** on pages (e.g. entering text, clicking buttons)
3. **Validates**:
    - That expected values appear on the page
    - That navigation leads to the correct page

QED’s DSL is built around these core ideas — no more, no less.

---

## Example: Basic UI Flow

Here’s a minimal DSL script that demonstrates navigation, interaction, and validation:

```kotlin
hasBrowser?.apply {
    navigateToApp()
    startFromPage(landingPage) {
        textInput.click()
        onPage(textInputPage) {
            textInput = "John Wilson"
            button.click()
            verify("button text should be the same as text input ('${textInput}')") {
                expect(textInput).to.equal(button.text)
            }
        }
    }
}
```
## What This Does
navigateToApp() opens the application URL

startFromPage(...) scopes the test to a known starting point

onPage(...) asserts that navigation succeeded

verify(...) performs an expectation with a human-readable description

## Composability & Reuse
QED encourages modular, reusable test components:

Page objects can be composed from shared screen areas

Actions and assertions are fluent and chainable

Test data can be injected or generated dynamically

## API & Hybrid Support
API tests use similar DSL constructs:
```kotlin
val json = " {\"title\":\"create todo process payroll\", \"doneStatus\": true,\"description\":\"description\" }"
val result = rest.send(RequestType.POST, APIChalURLPath.SIM_ENTITIES, json, 201)
logger.info { result }
verify("check response body") {
    expect(result.get("name").asText()).to.equal("bob")
    expect(result.get("id").asInt()).to.equal(11)
}

```
Hybrid tests can mix UI and API seamlessly:
```kotlin
val payroll = Payroll("create todo process payroll", true, "todo list needs to be added")
val json = QEDJson.toJson(payroll)
logger.info { "opening UI Testing Playground" }
val landingPage = GenericPage(UITestingPage(this))
hasBrowser?.apply {
    navigateToApp()
    startFromPage(landingPage) {
        val result = rest.send(RequestType.POST, APIChalURLPath.SIM_ENTITIES, json, 201)
        logger.info { "API test within ui test" }
        logger.info { result }
        verify("check response body") {
            expect(result.get("name").asText()).to.equal("bob")
            expect(result.get("id").asInt()).to.equal(11)
        }
    }
}
```

