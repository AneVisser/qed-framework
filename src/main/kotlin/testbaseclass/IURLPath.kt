package qed.testbaseclass

import kotlin.reflect.KClass

interface IURLPath {
    val route : String
    val method: RequestType
    val responseKind: PayloadKind?
    val payloadKind: PayloadKind?

    // Helper to build route with path params
    fun buildRoute(pathParams: Map<String, Any>): String {
        var result = route
        pathParams.forEach { (key, value) ->
            result = result.replace("{$key}", value.toString())
        }
        // remove duplicate slashes:
        return result.replace(Regex("(?<!:)//+"), "/")
    }
}

sealed class PayloadKind {
    data class Single(val type: KClass<*>) : PayloadKind()
    data class ListOf(val type: KClass<*>) : PayloadKind()
    data class ParameterizedOf(val outerType: Class<*>, val innerType: Class<*>) : PayloadKind()
}


