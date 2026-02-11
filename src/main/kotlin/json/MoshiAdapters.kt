package qed.json

import com.squareup.moshi.*
import java.math.BigDecimal
import java.util.*

class DoubleJsonAdapter {
    @ToJson
    fun toJson(writer: JsonWriter, value: Double?) {
        with(writer) {
            serializeNulls = true
            jsonValue(value)
        }
    }
    @FromJson
    fun fromJson(value: String): Double = value.toDouble()
}

class IntJsonAdapter {
    @ToJson
    fun toJson(writer: JsonWriter, value: Int?) {
        with(writer) {
            serializeNulls = true
            jsonValue(value)
        }
    }
    @FromJson
    fun fromJson(value: String?): Int? = value?.toInt()
}


class UuidAdapter : JsonAdapter<UUID>() {
    @FromJson
    override fun fromJson(reader: JsonReader): UUID? = UUID.fromString(reader.readJsonValue().toString())

    @ToJson
    override fun toJson(writer: JsonWriter, value: UUID?) {
        writer.jsonValue(value.toString())
    }
}


class BigDecimalJsonAdapter {
    // Converts BigDecimal to its string representation
    @ToJson
    fun toJson(writer: JsonWriter, value: BigDecimal) {
        writer.jsonValue(value)
    }

    // Constructs a BigDecimal from the string representation
    @FromJson
    fun fromJson(value: String): BigDecimal {
        return BigDecimal(value)
    }
}

object QEDJsonParser {
    fun parse(reader: JsonReader): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()
        reader.beginObject()
        while (reader.hasNext()) {
            val name = reader.nextName()
            result[name] = readValue(reader)
        }
        reader.endObject()
        return result
    }

    private fun readObject(reader: JsonReader): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()
        reader.beginObject()
        while (reader.hasNext()) {
            val name = reader.nextName()
            result[name] = readValue(reader)
        }
        reader.endObject()
        return result
    }

    private fun readArray(reader: JsonReader): List<Any?> {
        val result = mutableListOf<Any?>()
        reader.beginArray()
        while (reader.hasNext()) {
            result.add(readValue(reader))
        }
        reader.endArray()
        return result
    }

    private fun readValue(reader: JsonReader): Any? {
        return when (reader.peek()) {
            JsonReader.Token.BEGIN_OBJECT -> readObject(reader)
            JsonReader.Token.BEGIN_ARRAY -> readArray(reader)
            JsonReader.Token.STRING -> reader.nextString()
            JsonReader.Token.NUMBER -> {
                val raw = reader.nextString()
                try {
                    when {
                        raw.contains('.') || raw.contains('e') || raw.contains('E') -> {
                            // Parse as double, handles scientific notation like 1e30
                            raw.toDouble()
                        }
                        raw.length < 10 -> raw.toIntOrNull() ?: raw.toLong()
                        else -> raw.toLong()
                    }
                } catch (e: NumberFormatException) {
                    // If parsing fails, return the raw string
                    // This shouldn't happen with valid JSON numbers, but provides a fallback
                    raw
                }
            }
            JsonReader.Token.BOOLEAN -> reader.nextBoolean()
            JsonReader.Token.NULL -> {
                reader.nextNull<Any?>()
                null
            }
            else -> throw JsonDataException("Unexpected token: ${reader.peek()}")
        }
    }
}


class QEDJsonMapAdapter : JsonAdapter<Map<String, Any?>>() {

    @FromJson
    override fun fromJson(reader: JsonReader): Map<String, Any?> {
        val map = QEDJsonParser.parse(reader)
        return map
    }

    @ToJson
    override fun toJson(writer: JsonWriter, value: Map<String, Any?>?) {
        throw UnsupportedOperationException("Serialization not supported")
    }
}

/**
 * Extension function for String, to convert to pretty and compact JSon
 * use: "{\"abc\": \"alphabet\"}".toPrettyJSon()
 * or: "{\"abc\": \"alphabet\"}".toCompactJSonOrNull()
 *
 */


fun String.toPrettyJson(): String = QEDJson.prettyJson(QEDJson.fromJson<Any>(this)!!)
fun String.toPrettyJsonOrNull(): String? = runCatching { toPrettyJson() }.getOrNull()

fun String.toCompactJson(): String = QEDJson.compactJson(QEDJson.fromJson<Any>(this)!!)
fun String.toCompactJsonOrNull(): String? = runCatching { toCompactJson() }.getOrNull()

fun LinkedHashMap<String, Any>.toPrettyJson(): String = QEDJson.prettyJson(this)
fun LinkedHashMap<String, Any>.toPrettyJsonOrNull(): String? = runCatching { toPrettyJson() }.getOrNull()

fun LinkedHashMap<String, Any>.toCompactJson(): String = QEDJson.compactJson(this)
fun LinkedHashMap<String, Any>.toCompactJsonOrNull(): String? = runCatching { toCompactJson() }.getOrNull()

fun Any?.toPrettyJson(): String = QEDJson.prettyJson(this!!)
fun Any?.toPrettyJsonOrNull(): String? = runCatching { toPrettyJson() }.getOrNull()

fun Any?.toCompactJson(): String = QEDJson.compactJson(this!!)
fun Any?.toCompactJsonOrNull(): String? = runCatching { toCompactJson() }.getOrNull()

fun Map<String, Any>.toPrettyJson(): String = QEDJson.prettyJson(this)
fun Map<String, Any>.toPrettyJsonOrNull(): String? = runCatching { toPrettyJson() }.getOrNull()

fun Map<String, Any>.toCompactJson(): String = QEDJson.compactJson(this)
fun Map<String, Any>.toCompactJsonOrNull(): String? = runCatching { toCompactJson() }.getOrNull()

/**
 * convert nodes to a given type, so they don't need to be cast in test scripts
 * You can use that as follows:
 * result.get("nutrients").asJsonArray().get(0).asJsonObject().get("periodvalues").asJsonArray().get(0)
 * There is a shortcut (see below), but in constructs like this, you will need these functions:
 *
 *  val contours = result.get("Contours").asJsonArray()
 *  verify("check if all contours are found") {
 *    expect(contours.size).to.equal(3)
 *  }
 *
 */

@Suppress("UNCHECKED_CAST")
fun Any?.asJsonObject() = this as Map<String, Any?>
fun Any?.asJsonArray() = this as List<Any?>
fun Any?.asJsonPrimitive() = this

/**
 * use shortcut node accessors, where the type of parameter of 'get' determines what the object type
 * is that it is processing.
 *
 * It is shortcut for this
 * result.get("nutrients").asJsonArray().get(0).asJsonObject().get("periodvalues").asJsonArray().get(0)
 * And you can use it as follows:
 * result.get("nutrients").get(0).get("periodvalues").get(0)
 */
fun Any?.get(key: Any): Any? = when {
    this is Map<*, *> && key is String -> this[key]
    this is List<*> && key is Int -> this.getOrNull(key)
    else -> throw IllegalArgumentException("Cannot access key '$key' on type ${this?.javaClass?.simpleName}")
}

sealed class ConversionResult<T> {
    data class Success<T>(val value: T) : ConversionResult<T>()
    data class Failure<T>(
        val reason: String,
        val original: Any?
    ) : ConversionResult<T>()
}


fun Any?.intValue(): Int {
    return when (this) {
        is String -> this.toInt()
        is Number -> this.toInt()
        is Boolean -> if (this) 1 else 0
        null -> 0
        else -> throw IllegalArgumentException("Cannot convert ${this::class.simpleName} to Int")
    }
}

fun Any?.doubleValue(): Double {
    return when (this) {
        is String -> this.toDouble()
        is Number -> this.toDouble()
        is Boolean -> if (this) 1.0 else 0.0
        null -> 0.0
        else -> throw IllegalArgumentException("Cannot convert ${this::class.simpleName} to Int")
    }
}

class StrictAnyAdapter {
    @FromJson
    fun fromJson(reader: JsonReader): Any? {
        return when (reader.peek()) {
            JsonReader.Token.BEGIN_OBJECT -> {
                val map = mutableMapOf<String, Any?>()
                reader.beginObject()
                while (reader.hasNext()) {
                    val name = reader.nextName()
                    map[name] = fromJson(reader)
                }
                reader.endObject()
                map
            }
            JsonReader.Token.BEGIN_ARRAY -> {
                val list = mutableListOf<Any?>()
                reader.beginArray()
                while (reader.hasNext()) {
                    list.add(fromJson(reader))
                }
                reader.endArray()
                list
            }
            JsonReader.Token.STRING -> reader.nextString()
            JsonReader.Token.BOOLEAN -> reader.nextBoolean()
            JsonReader.Token.NULL -> reader.nextNull<Any>()
            JsonReader.Token.NUMBER -> {
                val s = reader.nextString()
                if (s.contains('.') || s.contains('e') || s.contains('E')) {
                    s.toDouble()
                } else {
                    // Use Long to cover large ints
                    val l = s.toLong()
                    // Optionally downcast if safe
                    if (l in Int.MIN_VALUE..Int.MAX_VALUE) l.toInt() else l
                }
            }
            else -> throw JsonDataException("Unexpected token: ${reader.peek()}")
        }
    }
}