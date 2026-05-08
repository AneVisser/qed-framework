# Behaviour-Driven Style (Without the Baggage)

QED supports **Given–When–Then** semantics for structuring tests — but without the constraints and overhead of traditional BDD frameworks like Cucumber.

---

## Why This Matters

Behaviour-driven syntax can improve readability and communication, especially across teams. But in many frameworks it comes at a cost:

- Rigid step definitions
- Regex-based matching
- Fragmented glue code
- Unwieldy test suites

QED takes a different approach: expressive, not prescriptive.

---

## How It Works

In QED, `Given`, `When`, and `Then` are expressive constructs that help organise test logic and clarify intent — without dictating how the test is implemented. There are no external step definitions, no glue code, and no magic matching. Just clean, idiomatic Kotlin:

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

---

## Reporting Integration

QED's reporting system captures each `Given`, `When`, and `Then` block as a distinct step in the test report. Descriptions are shown verbatim, nested actions are listed with timestamps and outcomes, and failures are scoped to the relevant block.

The report is generated at:

```
build/test-output/ExtentReport/TestExecutionReport.html
```

---

## Use It When It Helps

Behaviour-driven syntax is optional in QED. Use it when it improves clarity, skip it when it doesn't. It can be freely mixed with other DSL constructs:

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

Readability should never come at the cost of maintainability. Behaviour-driven syntax is a tool — not a constraint. It's there to help tell the story of your test, not to dictate how you write it.