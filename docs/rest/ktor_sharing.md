The suite has been setup so that URL paths, and associated information can be shared with a real application.
That way, we can build an integration-safe architecture. This means a Ktor client and Ktor routes 
always operate on the same typed definitions that have already been validated in your test framework.

That way, the test framework is turned into a verified contract layer between your Ktor app and the REST endpoints 
and we know that all endpoints used in the application have valid tests associated with them.
If not, it can be seen from the IURLPath enum type for the application where unused records are grayed out.
The base structure for this is:


```kotlin
interface IURLPath {
    val route : String
    val method: RequestType
    val responseKind: PayloadKind?
    val payloadKind: PayloadKind?
}

sealed class PayloadKind {
    data class Single(val type: KClass<*>) : PayloadKind()
    data class ListOf(val type: KClass<*>) : PayloadKind()
}

```
IURLPath cantains the url path or route that is used for a base url to define the endpoint.
An implementation of an enum type containing all available routes in an applcation coule look as follows:

```kotlin
enum class APIChalURLPath(private val path: String,
                          override val method: RequestType,
                          override val responseKind:PayloadKind?,
                          override val payloadKind: PayloadKind?,
)
    : IURLPath {
    POST_SIM_ENTITIES("/sim/entities", RequestType.POST, Single(Resp_SimEntities::class), Single(SimEntities::class)),
    GET_SIM_ENTITIES("/sim/entities", RequestType.GET, null, null),
    PUT_SIM_ENTITIES("/sim/entities", RequestType.PUT, null, Single(SimEntities::class)),
    TODOS("/todos", RequestType.GET, null, null)
    ;

    override val route: String
        get() = this.path
}
```

The method describes the method that is used when the URLPath record is passed to
the RestClient send method. The path or route is what is added to the base url so that the endpoint can be formed.

The data class type can also be passed to the URL record. That way, we can make a type-safe call to
the API and not accidentally pass in the wrong Json. If the payloadKind is not null, the send method verifies if the right
data type is passed. if not, the test will fail.
The same applies to the response. This can also be typed, so that we know that the response from the 
serveris cast into the correct record type.

The following example shows how that can be done:
```kotlin
data class SimEntities (
    val title : String,
    val doneStatus: Boolean,
    val description : String
)
data class Resp_SimEntities(
    val id : Int,
    val name : String,
    val description: String
)

fun PostTest() {
    val simEntities = SimEntities("create todo process payroll", true, "description")
    val result : Resp_SimEntities = rest.send(APIChalURLPath.POST_SIM_ENTITIES, simEntities, listOf(201), trackPerformance = true)
    logger.info { result }
    verify("check response body") {
        expect((result).name).to.equal("bob")
        expect((result).id).to.equal(11)
    }
}
```

The rest.send function is able to accept the correct data type. There is also a sendUntyped. This can still receive a typed
payload, but will return a Map<String, Any>> as response.