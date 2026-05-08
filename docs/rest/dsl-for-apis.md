# DSL for APIs

QED's DSL for API testing is designed to be expressive, readable, and minimal — reflecting the core actions testers perform when interacting with RESTful services. Whether you're validating a simple GET or orchestrating a complex POST with dynamic payloads, the DSL keeps your tests declarative and intention-focused.

---

## Core Construct: `rest.send(...)`

This is the primary entry point for API interactions:

```kotlin
val result = rest.sendUntyped(
    method = RequestType.POST,
    endpoint = APIChalURLPath.SIM_ENTITIES,
    payload = json,
    expectedStatus = 201
)
```

- `method` — HTTP verb (`GET`, `POST`, `PUT`, `DELETE`, etc.)
- `endpoint` — enum or sealed class representing the API path
- `payload` — JSON string or serialized object
- `expectedStatus` — expected HTTP response code

---

## Example: Basic POST Request

```kotlin
val json = """{ "name": "bob", "id": 11 }"""
val result = rest.sendUntyped(RequestType.POST, APIChalURLPath.SIM_ENTITIES, json, 201)

verify("check response body") {
    expect(result.get("name").asText()).to.equal("bob")
    expect(result.get("id").asInt()).to.equal(11)
}
```

- Use `verify { ... }` blocks to group expectations
- Access response fields with `.get("fieldName")`
- Use `.asText()`, `.asInt()`, etc. for type conversion

---

## Hybrid Flow Integration

API steps can be embedded within UI flows, allowing mixed browser and API interactions in a single test:

```kotlin
startFromPage(landingPage) {
    val result = rest.sendUntyped(RequestType.POST, APIChalURLPath.SIM_ENTITIES, json, 201)
    verify("check response body") {
        expect(result.get("name").asText()).to.equal("bob")
        expect(result.get("id").asInt()).to.equal(11)
    }
}
```