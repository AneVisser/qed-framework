# Test-Driven Development (TDD) Workflow

QED is designed to support test-first development — where the test defines the desired behavior before the system under test (SUT) is implemented. This workflow encourages clarity, confidence, and rapid iteration, especially for RESTful services.

---

## Philosophy

In QED, the test is the specification.

You write the test first — expressing what the API should do, how it should respond, and what values it should return. Then you implement the service logic until the test passes.

This approach:

- Keeps development focused on outcomes
- Encourages minimal, intention-revealing code
- Provides instant feedback during implementation

---

## Workflow Steps

1. **Define the expected behavior** in a QED test
2. **Run the test** — it will fail (the endpoint doesn’t exist yet)
3. **Implement the endpoint logic** in your REST service
4. **Re-run the test** until it passes
5. **Track performance** with `trackPerformance = true` (optional)

---

## Example: Creating a Todo

Let’s say we want to POST a new todo item and expect a specific response.

### Step 1: Write the Test

```kotlin
val json = """{ "title": "process payroll", "doneStatus": true, "description": "monthly task" }"""
val result = rest.sendUntyped(RequestType.POST, APIChalURLPath.TODO_LIST, json, 201)

verify("response should contain expected fields") {
    expect(result.get("title").asText()).to.equal("process payroll")
    expect(result.get("doneStatus").asBoolean()).to.equal(true)
    expect(result.get("description").asText()).to.equal("monthly task")
}
```
### Step 2: Implement the Endpoint
In your REST service, create the /todos POST handler.
Return a JSON response that matches the test expectations.

### Step 3: Iterate
Run the test again. Fix any mismatches in field names, types, or status codes. 
The test acts as your guide

### Step 4: Validate and Extend
Once the test passes, you can:
- Add more assertions
- Write additional tests for edge cases
- Track performance with:
```kotlin
val result = rest.sendUntyped(RequestType.POST, APIChalURLPath.TODO_LIST, json, 201, trackPerformance = true)
```

## Tips for TDD with QED
- Use descriptive verify(...) messages to clarify intent
- Keep payloads minimal and focused
- Use enums or sealed classes for endpoint paths
- Prefer Moshi serialization for type safety
- Run tests frequently — QED is built for fast feedback

## Optional: Performance Tracking

TDD isn’t just about correctness — it’s also about responsiveness. 
By enabling trackPerformance = true, you can monitor endpoint latency 
as part of your test suite.

This integrates with QED’s performance summary and history reports.

## Reusability
Wrap common test patterns into helper functions:
```kotlin
fun createTodo(title: String): JsonNode {
    val todo = Todo(title, done = false, description = "auto-generated")
    val json = moshi.adapter<Todo>().toJson(todo)
    return rest.sendUntyped(RequestType.POST, APIChalURLPath.TODO_LIST, json, 201)
}
```