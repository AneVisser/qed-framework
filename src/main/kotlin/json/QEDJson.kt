package qed.json

import com.squareup.moshi.*
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okio.Buffer
import org.json.JSONArray
import java.io.File
import kotlin.reflect.KClass

/**
 * Singleton to process json structures
 * syntax to use is:
 *
 * val json = QEDJson.toJson(MyPayload(...))        // for known types
 * val obj = QEDJson.fromJson<MyPayload>(json)      // for known types
 * val obj = QEDJson.mapFromJson(json)              // for unknown types
 */

object QEDJson {

    val smartMapType = Types.newParameterizedType(
        Map::class.java,
        String::class.java,
        Any::class.java
    )


    val moshi: Moshi = Moshi.Builder()
        .add(smartMapType, QEDJsonMapAdapter()) // must be added before KotlinJsonAdapterFactory
        .add(DoubleJsonAdapter())
        .add(IntJsonAdapter())
        .add(UuidAdapter())
        .add(BigDecimalJsonAdapter())
        .add(StrictAnyAdapter())
        .add(KotlinJsonAdapterFactory())
        .build()

    // use this for typed, schema-driven parsing, typical for configuration
    // files or json files with a known structure
    inline fun <reified T> fromJson(json: String): T? =
        moshi.adapter(T::class.java).fromJson(json)

    inline fun <reified T> toJson(value: T): String =
        moshi.adapter(T::class.java).toJson(value)

    inline fun <reified T> toJson(value: List<T>): String {
        val type = Types.newParameterizedType(List::class.java, T::class.java)
        val adapter = moshi.adapter<List<T>>(type)
        return adapter.toJson(value)
    }

    inline fun <reified T> fromJsonList(json: String): List<T>? {
        val type = Types.newParameterizedType(List::class.java, T::class.java)
        val adapter = moshi.adapter<List<T>>(type)
        return adapter.fromJson(json)
    }

    /** Decode a JSON string into a single object, where the type is only known at runtime. */
    fun <T : Any> fromJson(kclass: KClass<T>, jsonString: String): T? =
        try {
            moshi.adapter(kclass.java).fromJson(jsonString)
        } catch (e: Exception) {
            throw IllegalArgumentException("Failed to decode JSON into ${kclass.simpleName}: ${e.message}", e)
        }

    /** Decode a JSON string into a list of objects, where the element type is only known at runtime. */
    fun <T : Any> fromJsonList(kclass: KClass<T>, jsonString: String): List<T>? {
        try {
            val type = Types.newParameterizedType(List::class.java, kclass.java)
            val adapter = moshi.adapter<List<T>>(type)
            return adapter.fromJson(jsonString)
        } catch (e: Exception) {
            throw IllegalArgumentException("Failed to decode JSON into ${kclass.simpleName}: ${e.message}", e)
        }
    }

    fun prettyJson(value: Any): String {
        val buffer = Buffer()
        val writer = JsonWriter.of(buffer).apply { setIndent("  ") }
        val adapter = moshi.adapter(Any::class.java)
        adapter.toJson(writer, value)
        return buffer.readUtf8()
    }

    fun compactJson(value: Any): String {
        val buffer = Buffer()
        val writer = JsonWriter.of(buffer).apply { setIndent("") } // no indentation
        moshi.adapter(Any::class.java).toJson(writer, value)
        return buffer.readUtf8()
    }

    fun writeToFile(file: File, value: Any) {
        file.writeText(prettyJson(value))
    }

    // use this for flexible traversal or partial validation, typical for API responses
    @Suppress("UNCHECKED_CAST")
    fun mapFromJson(json: String): Any? {
        val adapter = moshi.adapter<Any>(Any::class.java)
        val parsed = adapter.fromJson(json)
        return when (parsed) {
            is Map<*, *> -> parsed.asMap()
            is List<*> -> parsed.asList()
            else -> null
        }
    }


    fun List<String>.toMetadataList(source: String = "page_object"): List<Map<String, String>> =
        map { mapOf("source" to source) }

    inline fun jsonPayload(block: MutableMap<String, Any?>.() -> Unit): Map<String, Any?> =
        mutableMapOf<String, Any?>().apply(block)

    fun List<*>.toJsonArray(): JSONArray = JSONArray(this)

    @Suppress("UNCHECKED_CAST")
    fun Any?.asMap(): Map<String, Any>? =
        this as? Map<String, Any>

    @Suppress("UNCHECKED_CAST")
    fun Any?.asList(): List<Any>? =
        when (this) {
            is List<*> -> this as List<Any>
            is Map<*, *> -> (this as Map<String, Any>)["items"] as? List<Any>
            else -> null
        }


    fun <T : Any> decodeSafely(type: KClass<T>, json: String): T {
        val adapter = moshi.adapter(type.java)
        try {
            val trimmed = json.trim()
            val firstChar = trimmed.firstOrNull()
            val rootType = when (firstChar) {
                '{' -> "JsonObject"
                '[' -> "JsonArray"
                else -> "Unknown"
            }

            // Print JSON info like Ktor app
            println("🔍 Attempting to deserialize as Single(${type.simpleName})")
            println("📦 Response length: ${json.length} characters")
            println("📄 Response preview (first 500 chars):\n${json.take(500)}")
            println("✅ JSON is valid. Root type: $rootType")

            if (rootType == "JsonObject") {
                try {
                    val jsonObj = com.squareup.moshi.JsonReader.of(okio.Buffer().writeUtf8(trimmed)).readJsonValue() as? Map<*, *>
                    if (jsonObj != null) {
                        println("📋 Top-level keys: ${jsonObj.keys.take(10)}")
                    }
                } catch (_: Exception) { /* ignore */ }
            }

            val result = adapter.fromJson(json) ?: throw JsonDataException("Decoded value is null")
            println("✅ Successfully deserialized to ${result::class.simpleName}")
            return result
        } catch (e: Exception) {
            println("❌ Error decoding ${type.simpleName}: ${e.message}")
            throw e
        }
    }

    fun <T : Any> decodeSafelyList(elementType: KClass<T>, json: String): List<T> {
        val listType = Types.newParameterizedType(List::class.java, elementType.java)
        val adapter = moshi.adapter<List<T>>(listType)
        try {
            println("🔍 Attempting to deserialize as ListOf(${elementType.simpleName})")
            println("📦 Response length: ${json.length} characters")
            println("📄 Response preview (first 500 chars):\n${json.take(500)}")
            val list = adapter.fromJson(json) ?: emptyList()
            println("✅ Successfully deserialized List<${elementType.simpleName}> with ${list.size} elements")
            return list
        } catch (e: Exception) {
            println("❌ Error decoding List<${elementType.simpleName}>: ${e.message}")
            throw e
        }
    }


}