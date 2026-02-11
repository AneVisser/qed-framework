QED provides a set of expressive, Moshi-powered utilities for parsing, 
formatting, and verifying JSON structures. 
These utilities are based on logic from Moshi with a lean, 
type-safe, and DSL-friendly approach.

## Core functions
- QEDJson.fromJson<T>(json: String): T?
  <i>Parses a JSON string into a Kotlin object of type T.</i>

- QEDJson.toJson<T>(json: String): T?
  <i>Kotlin JSON object (Map<String, Any?>) to String.</i>

- QEDJson.prettyJson(value: Any): String
  <i>Serializes any Kotlin object into indented, human-readable JSON.</i>

- QEDJson.compactJson(value: Any): String
  <i>Serializes any Kotlin object into compact, minified JSON.</i>

- QEDJson.writeToFile(file: File, value: Any): String
  <i>Writes any Kotlin object to a file.</i>

- QEDJson.mapFromJson(json : String): Map<String, Any>?
  <i>Converts a json string to an object of type Map<String, Any>?.</i>



## Extension functions
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

## Type-safe traversal
Can be used when traversing a nested Json object, for example:

val value = jsonResult.get("nutrients").get(0).get("periodvalues").get(0)

```kotlin
fun Any?.get(key: Any) : Any?
```

## Type helpers
This syntax supports deep verification of nested JSON structures 
with minimal boilerplate. For example:

```kotlin
expect(result.get("nutrients").asJsonArray().get(0).get("periodvalues").asJsonObject().get(0).asJsonObject().get("nutvalue")).to.equal(40.0)
```
As the 'get' function is overloaded, the parameter passed to the get function determines which overloeaded function 
will be used. So if you use .get(0), the Json structure it pertains to is
automatically a JSonArray, whereas if you use get("nutrients"), it will automatically
be a JsonObject. So the above statement could be reduced to:

```kotlin
expect(result.get("nutrients").get(0).get("periodvalues").get(0).get("nutvalue")).to.equal(40.0)
```

```kotlin
fun Any?.asJsonArray(): List<Any?>
fun Any?.asJsonObject(): Map<String, Any?>
fun Any?.asJsonPrimitive(): Any?
```

## For type-safe formatting and logging:
```kotlin
fun Any?.toPrettyJsonOrNull(): String?
fun Any?.toCompactJsonOrNull(): String?
```

## Usage

```json
{"nutrientlist": [{"nutcode": "ME"}, {"nutcode":"NE"}, {"nutcode": "CP"}]}
```
can be accessed by:
```kotlin
expect(result.get("nutrientlist").get(0).get("nutcode")).to.equal("ME")
```


```kotlin
result.toPrettyJsonOrNull()
```
will produce:
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

