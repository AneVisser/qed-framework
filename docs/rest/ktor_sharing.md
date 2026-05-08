# Sharing with Ktor

The suite has been set up so that URL paths and associated metadata can be shared with a Ktor application, enabling an integration-safe architecture. This means a Ktor client and Ktor routes always operate on the same typed definitions that have already been validated in your test framework.

The test framework becomes a verified contract layer between your Ktor app and its REST endpoints — ensuring that all endpoints used in the application have valid tests associated with them. Unused records in the `IURLPath` enum will be visibly grayed out in the IDE.

---

## Base Structure

```kotlin
interface IURLPath {
    val route: String
    val method: RequestType
    val responseKind: PayloadKind?
    val payloadKind: PayloadKind?
}

sealed class PayloadKind {
    data class Single(val type: KClass<*>) : PayloadKind()
    data class ListOf(val type: KClass<*>) : PayloadKind()
}
```

`IURLPath` contains the URL path or route that is combined with a base URL to form the full endpoint. An implementation containing all available routes in an application could look as follows:

```kotlin
enum class APIChalURLPath(
    private val path: String,
    override val method: RequestType,
    override val responseKind: PayloadKind?,
    override val payloadKind: PayloadKind?,
) : IURLPath {
    POST_SIM_ENTITIES("/sim/entities", RequestType.POST, Single(Resp_SimEntities::class), Single(SimEntities::class)),
    GET_SIM_ENTITIES("/sim/entities", RequestType.GET, null, null),
    PUT_SIM_ENTITIES("/sim/entities", RequestType.PUT, null, Single(SimEntities::class)),
    TODOS("/todos", RequestType.GET, null, null);

    override val route: String
        get() = this.path
}
```

`method` describes the HTTP verb used when the URL path record is passed to the `RestClient` send method. `route` is appended to the base URL to form the full endpoint.

---

## Type-Safe Payloads and Responses

Data class types can be declared directly on the URL record, enabling type-safe API calls. If `payloadKind` is not null, the send method verifies that the correct data type is passed — if not, the test will fail. The same applies to the response: declaring `responseKind` ensures the server response is cast into the correct type automatically.

```kotlin
data class SimEntities(
    val title: String,
    val doneStatus: Boolean,
    val description: String
)

data class Resp_SimEntities(
    val id: Int,
    val name: String,
    val description: String
)

fun PostTest() {
    val simEntities = SimEntities("create todo process payroll", true, "description")
    val result: Resp_SimEntities = rest.send(APIChalURLPath.POST_SIM_ENTITIES, simEntities, listOf(201), trackPerformance = true)
    logger.info { result }
    verify("check response body") {
        expect(result.name).to.equal("bob")
        expect(result.id).to.equal(11)
    }
}
```

---

## Typed vs Untyped Send

`rest.send` accepts a typed payload and returns a typed response as declared on the URL path record.

`rest.sendUntyped` also accepts a typed payload, but returns a `Map<String, Any>` regardless of the declared response type — useful for exploratory or schema-agnostic tests.

See also [REST Setup](rest-setup.md) for the shared directory structure that makes this pattern possible.