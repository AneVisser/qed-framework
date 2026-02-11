## Behavior-Driven Style (Without the Baggage)

QED supports **Given–When–Then** semantics for structuring tests — but without the constraints and overhead of traditional BDD frameworks like Cucumber.

---

## Why This Matters

Behavior-driven syntax can improve readability and communication, especially across teams. But in many frameworks, it comes at a cost:

- Rigid step definitions
- Regex-based matching
- Fragmented glue code
- Unwieldy test suites

QED takes a different approach.

---

## Expressive, Not Prescriptive

In QED, `Given`, `When`, and `Then` are expressive constructs that help organize test logic and clarify intent — without dictating how the test is implemented.

```kotlin
Given(this, "I log in to MyApp") {
    login(context, "name", "pw")
}
When(this, "I create an invoice") {
    invoice = createInvoice(context, clientNr)
}
Then(this, "I can navigate to the invoice") {
    searchInvoice(context, invoice)
}
```
- No external step definitions
- No glue code
- No magic matching
- Just clean, idiomatic Kotlin.

## Reporting Integration
QED’s reporting system (e.g. ExtentReports) captures each Given, When, and Then block as a distinct step in the test report:

Descriptions are shown verbatim

Nested actions are listed with timestamps and outcomes

Failures are scoped to the relevant block

You can find the report at:
```html
build/test-output/ExtentReport/TestExecutionReport.html
```
## Use It When It Helps
Behavior-driven syntax is optional in QED. Use it when it improves clarity, skip it when it doesn’t. You can mix and match with other DSL constructs:
```kotlin
startFromPage(homePage) {
    button.click()
    onPage(detailsPage) {
        verify("details are visible") {
            expect(detailsPanel.isVisible).to.be(true)
        }
    }
}
```
## Philosophy
QED believes that readability should never come at the cost of maintainability. Behavior-driven syntax is a tool — not a framework. It’s there to help you tell the story of your test, not to constrain how you write it.