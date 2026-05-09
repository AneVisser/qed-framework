# JSON Utilities

QED provides a set of expressive, Moshi-powered utilities for parsing, formatting, and verifying JSON structures. The approach is lean, type-safe, and DSL-friendly.

---

## Core Functions

**`QEDJson.fromJson<T>(json: String): T?`**
Parses a JSON string into a Kotlin object of type `T`.

**`QEDJson.toJson<T>(value: T): String`**
Serializes a Kotlin object of type `T` to a JSON string.

**`QEDJson.prettyJson(value: Any): String`**
Serializes any Kotlin object into indented, human-readable JSON.

**`QEDJson.compactJson(value: Any): String`**
Serializes any Kotlin object into compact, minified JSON.

**`QEDJson.writeToFile(file: File, value: Any): String`**
Writes any Kotlin object to a file as JSON.

**`QEDJson.mapFromJson(json: String): Map<String, Any>?`**
Converts a JSON string to a `Map<String, Any>?`.

---

## Extension Functions

```kotlin
fun String.toPrettyJson(): String
fun String.toPrettyJsonOrNull(): String?

fun String.toCompactJson(): String
fun String.toCompactJsonOrNull(): String?

fun LinkedHashMap<String, Any>.toPrettyJson(): String
fun LinkedHashMap<String, Any>.toPrettyJsonOrNull(): String?

fun LinkedHashMap<String, Any>.toCompactJson(): String
fun LinkedHashMap<String, Any>.toCompactJsonOrNull(): String?

fun Any?.toPrettyJson(): String
fun Any?.toPrettyJsonOrNull(): String?

fun Any?.toCompactJson(): String
fun Any?.toCompactJsonOrNull(): String?

fun Map<String, Any>.toPrettyJson(): String
fun Map<String, Any>.toPrettyJsonOrNull(): String?

fun Map<String, Any>.toCompactJson(): String
fun Map<String, Any>.toCompactJsonOrNull(): String?
```

---

## Type-Safe Traversal

The `get` function is overloaded to support clean traversal of nested JSON structures:

```kotlin
fun Any?.get(key: Any): Any?
```

The parameter type determines which overload is used — passing a `String` key treats the structure as a JSON object, passing an `Int` index treats it as a JSON array. This means verbose type casts can be eliminated entirely:

```kotlin
// Without type-safe traversal
expect(result.get("nutrients").asJsonArray().get(0).get("periodvalues").asJsonObject().get(0).asJsonObject().get("nutvalue")).to.equal(40.0)

// With type-safe traversal
expect(result.get("nutrients").get(0).get("periodvalues").get(0).get("nutvalue")).to.equal(40.0)
```

---

## Type Helpers

```kotlin
fun Any?.asJsonArray(): List<Any?>
fun Any?.asJsonObject(): Map<String, Any?>
fun Any?.asJsonPrimitive(): Any?
```

---

## Usage Example

Given the following JSON response:

```json
{"nutrientlist": [{"nutcode": "ME"}, {"nutcode": "NE"}, {"nutcode": "CP"}]}
```

Access a nested value:

```kotlin
expect(result.get("nutrientlist").get(0).get("nutcode")).to.equal("ME")
```

Pretty-print the full result for logging or debugging:

```kotlin
result.toPrettyJsonOrNull()
```

Produces:

```json
{
    "nutrientlist": [
        {
            "nutcode": "ME"
        },
        {
            "nutcode": "NE"
        },
        {
            "nutcode": "CP"
        }
    ]
}
```