package qed.testbaseclass

import com.squareup.moshi.Types
import io.restassured.http.ContentType
import io.restassured.module.kotlin.extensions.Extract
import io.restassured.module.kotlin.extensions.Given
import io.restassured.module.kotlin.extensions.Then
import io.restassured.module.kotlin.extensions.When
import io.restassured.response.Response
import org.hamcrest.Matchers.anyOf
import org.hamcrest.Matchers.equalTo
import qed.json.QEDJson
import qed.json.toCompactJson
import qed.performance.trackPerf
import qed.reports.Logger
import java.net.HttpURLConnection
import java.net.URI
import java.net.URL
import java.net.URLEncoder
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration
import kotlin.reflect.KClass

/**
 * The extract block in executeSend can be customised, so that responses that aren't in standard JSON can be processed and returned as JSON.
 * So for example, Ollama has a specialised handler so that it can process the streamed returns from the server and parse the response into a single string.
 */
enum class ExtractStrategy(
    val extractor: (Response) -> Any?
) {
    DEFAULT({ response ->
        // Default JSON parsing
        val json = response.body().asString()
        val cleanJson = json.replace(Regex("""(\r\n)|\n"""), "")
        QEDJson.mapFromJson(cleanJson)!!
    }),
    OLLAMA_STREAM({ response ->
        // Ollama streaming JSON parser
        val raw = response.body().asString()
        val sb = StringBuilder()
        raw.lines().forEach { line ->
            if (line.isBlank()) return@forEach
            val chunk = Regex("\"response\"\\s*:\\s*\"(.*?)\"").find(line)?.groups?.get(1)?.value
            if (chunk != null) sb.append(chunk)
        }
        mapOf<String, Any>("response" to sb.toString())
    })
}


class RestClient(var url : String, val logger : Logger, val baseTest: BaseTest) : IREST {
    // add a parameter that is part of a path, so single parameter delimited by slash
    override fun addPathParameters(parameters: List<String>): String {
        return parameters.joinToString("/") { URLEncoder.encode(it, "UTF-8") }
    }

    /**
     * The send function has the option to track performance. If you want to use that, you need to set trackPerformance
     * to true in the call site
     */
    override fun sendUntyped(
        urlPath: IURLPath,
        body: Any?,
        statusCodeLst: List<Int>,
        pathParams: Map<String, Any>?,  // NEW
//        pathParameters: List<String>?,
        parameterPairs: List<URLParameter>?,
        contentType: ContentType,
        headerLst : List<Pair<String, String>>?,
        extractStrategy: ExtractStrategy,
        trackPerformance: Boolean
    ): Any {
        // Build the full path
        val fullPath = if (pathParams != null && pathParams.isNotEmpty()) {
            // NEW: Use path params to replace placeholders
            urlPath.buildRoute(pathParams)
        } else {
            // No parameters
            urlPath.route
        }
        lateinit var returnJSON: Any
        val method = urlPath.method
        // Payload type validation (unchanged)
        if (body != null) {
            when (val kind = urlPath.payloadKind) {
                is PayloadKind.Single ->
                    require(kind.type.isInstance(body)) {
                        "Invalid payload type for ${urlPath.route}. Expected ${kind.type.simpleName}, got ${body::class.simpleName}"
                    }
                is PayloadKind.ListOf -> {
                    require(body is List<*>) { "Expected a List for ${urlPath.route}, got ${body::class.simpleName}" }
                    if (body.isNotEmpty()) {
                        val first = body.first()
                        require(kind.type.isInstance(first)) {
                            "Invalid list element type for ${urlPath.route}. Expected ${kind.type.simpleName}, got ${first!!::class.simpleName}"
                        }
                    }
                }
                is PayloadKind.ParameterizedOf ->
                    // ParameterizedOf is a response wrapper type — no request body validation needed
                    Unit
                null -> if (urlPath.method == RequestType.POST || urlPath.method == RequestType.PUT)
                    logger.warn { "no PayloadKind defined for $urlPath" }
            }
        }

        // Execute REST request (unchanged)
        fun executeSend() =
            Given {
                contentType("application/json")
                if (method in arrayOf(RequestType.POST) && body != null) {
                    when (body) {
                        is String -> body(body)
                        else -> body(QEDJson.toJson(body))
                    }
                }
                basePath(fullPath)
                parameterPairs?.forEach {
                    queryParam(it.name, it.value)
                }

                contentType(contentType)
                headerLst?.forEach { pair ->
                    header(pair.first, pair.second)
                }
                baseUri(url)
            } When {
                when (method) {
                    RequestType.PUT -> put()
                    RequestType.GET -> get()
                    RequestType.POST -> post()
                    RequestType.DELETE -> delete()
                    RequestType.PATCH -> patch()
                }
            } Then {
                statusCode(anyOf(*statusCodeLst.map { equalTo(it) }.toTypedArray()))
            } Extract {
                val retJSON = extractStrategy.extractor(this as Response)
                returnJSON = retJSON ?: throw Exception("the request returned null")
            }

        if (trackPerformance)
            trackPerf(baseTest.uniqueClassName, baseTest.methodName, method, urlPath) {
                executeSend()
            }
        else
            executeSend()
        return returnJSON
    }


    /**
     * This send function expects either a class type for the response variable (which can be Single or ListOf). If not, it tries to
     * return the result from sendUntyped. In that case, the result is Map<String, Any?>
     */
    @Suppress("UNCHECKED_CAST")
    inline fun <reified T : Any> send(
        urlPath: IURLPath,
        body: Any? = null,
        statusCodeLst: List<Int> = listOf(200, 201),
        pathParams: Map<String, Any>? = null,  // NEW: replaces pathParameters list
//        pathParameters: List<String>? = null, // Keep for backward compatibility
        parameterPairs: List<URLParameter>? = null,
        contentType: ContentType = ContentType.JSON,
        headerLst : List<Pair<String, String>>? = null,
        extractStrategy: ExtractStrategy = ExtractStrategy.DEFAULT,
        trackPerformance: Boolean = false
    ): T {
        val rawResult = sendUntyped(
            urlPath = urlPath,
            body = body,
            statusCodeLst = statusCodeLst,
            pathParams = pathParams,
//            pathParameters = pathParameters,
            parameterPairs = parameterPairs,
            contentType = contentType,
            extractStrategy = extractStrategy,
            headerLst = headerLst,
            trackPerformance = trackPerformance
        )

        val json = rawResult.toCompactJson()

        return try {
            when (val kind = urlPath.responseKind) {
                is PayloadKind.Single -> {
                    QEDJson.decodeSafely(kind.type as KClass<T>, json)
                }
                is PayloadKind.ListOf -> {
                    val list = QEDJson.decodeSafelyList(kind.type, json)
                    list as T
                }
                is PayloadKind.ParameterizedOf -> {
                    val type = Types.newParameterizedType(kind.outerType, kind.innerType)
                    val adapter = QEDJson.moshi.adapter<T>(type)
                    adapter.fromJson(json) ?: throw IllegalStateException("Deserialization returned null for ${urlPath.route}")
                }
                else -> {
                    QEDJson.decodeSafely(T::class, json)
                }
            }
        } catch (e: Exception) {
            println("❌ Deserialization failed for ${urlPath.route}")
            throw e
        }
    }

    fun send(link: String, method: RequestType, payload: String? = null): String {
        val client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build()
        val requestBuilder = HttpRequest.newBuilder()
            .uri(URI.create(link))
            .timeout(Duration.ofSeconds(30))
            .header("Content-Type", "application/json")

        when (method) {
            RequestType.POST -> requestBuilder.POST(
                HttpRequest.BodyPublishers.ofString(payload ?: "{}")
            )
            RequestType.PUT -> requestBuilder.PUT(
                HttpRequest.BodyPublishers.ofString(payload ?: "{}")
            )
            RequestType.GET -> requestBuilder.GET()
            RequestType.DELETE -> requestBuilder.DELETE()
            else -> {
                throw Exception("Method ${method.name} not supported")
            }
        }
        val request = requestBuilder.build()
        val response = client.send(request, HttpResponse.BodyHandlers.ofString())
        return response.body()
    }

    // For tests where you need the status code too:
    data class HttpTestResponse(val statusCode: Int, val body: String)

    fun sendWithStatus(link: String, method: RequestType, payload: String? = null): HttpTestResponse {
        val client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build()
        val requestBuilder = HttpRequest.newBuilder()
            .uri(URI.create(link))
            .timeout(Duration.ofSeconds(30))
            .header("Content-Type", "application/json")
        when (method) {
            RequestType.POST -> requestBuilder.POST(
                HttpRequest.BodyPublishers.ofString(payload ?: "{}")
            )
            RequestType.PUT -> requestBuilder.PUT(
                HttpRequest.BodyPublishers.ofString(payload ?: "{}")
            )
            RequestType.GET -> requestBuilder.GET()
            RequestType.DELETE -> requestBuilder.DELETE()
            else -> {
                throw Exception("Method ${method.name} not supported")
            }
        }
        val request = requestBuilder.build()
        val response = client.send(request, HttpResponse.BodyHandlers.ofString())
        return HttpTestResponse(response.statusCode(), response.body())
    }

    /**
     * Use the json traversing function to evaluate if two json strings are the same.
     */
    override fun jsonVerify(expValue: String, testValue : String) {
        JsonVerify(expValue, testValue, logger)
    }

}
