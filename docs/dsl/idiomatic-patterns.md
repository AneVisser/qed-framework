# Idiomatic Patterns

These patterns reflect how QED is meant to be used — expressive, modular, and context-aware.

---

## Fluent Assertions

```kotlin
verify("username should match expected") {
    expect(user.name).to.equal("Alice")
    expect(user.roles).to.contain("admin")
}
```

- Use `verify` blocks to group related expectations
- Prefer descriptive strings that explain intent, not just what is being checked

---

## Page Composition

```kotlin
val loginPage = LoginPage(headerArea, formArea)
```

- Compose pages from reusable screen areas
- Avoid duplication by extracting shared regions into their own area classes

---

## Dynamic Data Injection

```kotlin
val user = generateUser(name = "Alice", role = "admin")
textInput = user.name
val json = QEDJson.toJson(user)
```

- Use data classes to represent test data
- Keep tests declarative — describe what should happen, not how to make it happen

---

## Hybrid Flow Integration

```kotlin
val result = rest.send(RequestType.POST, endpoints.NAME, json, 201)
onPage(dashboardPage) {
    verify("API result should appear in UI") {
        expect(result.get("name").asText()).to.equal(nameField.text)
    }
}
```

- Use enumerated types to define endpoints
- Mix API and UI steps seamlessly within a single flow
- Validate cross-layer consistency — assert that what the API returns is what the UI shows

---

## Composability Tips

- Prefer small, focused page objects over monoliths
- Use extension functions to simplify repetitive actions
- Group related verifications into named `verify` blocks for clarity in reports
- Avoid ceremony — if a construct feels verbose, it is a signal to refactor