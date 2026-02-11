## Fluent assertions

These patterns reflect how QED is meant to be used — expressive, modular, and context-aware.
```kotlin
verify("username should match expected") {
    expect(user.name).to.equal("Alice")
    expect(user.roles).to.contain("admin")
}
```
- Use verify blocks to group related expectations.
- Prefer descriptive strings over terse comments.

## Page composition

```kotlin
val loginPage = LoginPage(headerArea, formArea)
```
- Compose pages from reusable screen areas.
- Avoid duplication by extracting shared regions.

## Dynamic Data Injection
```kotlin
val user = generateUser(name = "Alice", role = "admin")
textInput = user.name
val json = QEDJson.toJson(user)
```
- Use data classes for test data.
- Keep tests declarative and intention-focused.

## Hybrid flow integration
```kotlin
val result = rest.send(RequestType.POST, endpoints.NAME, json, 201)
onPage(dashboardPage) {
    verify("API result should appear in UI") {
        expect(result.get("name").asText()).to.equal(nameField.text)
    }
}
```
- use enumerated types to define endpoints
- Seamlessly mix API and UI steps.
- Validate cross-layer consistency.

## Composability Tips
- Prefer small, focused page objects over monoliths.
- Use extension functions to simplify repetitive actions.
- Group related verifications into named blocks for clarity.
- Avoid ceremony — if a construct feels verbose, consider refactoring the DSL.