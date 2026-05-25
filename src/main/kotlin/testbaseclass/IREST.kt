package qed.testbaseclass

import io.restassured.http.ContentType

interface IREST {
    fun addPathParameters(parameters : List<String>) : String

    fun sendUntyped(urlPath: IURLPath,
                    body : Any? = null,
                    statusCodeLst : List<Int> = listOf(200),
                    pathParams: Map<String, Any>? = null,  // NEW
//                    pathParameters : List<String>? = null,
                    parameterPairs : List<URLParameter>? = null,
                    contentType : ContentType = ContentType.JSON,
                    headerLst : List<Pair<String, String>>? = null,
                    extractStrategy : ExtractStrategy  = ExtractStrategy.DEFAULT,
                    trackPerformance : Boolean = false) : Any?

    fun jsonVerify(expValue: String, testValue : String)

}

data class URLParameter (val name : String, val value : String)

