package qed.testbaseclass

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class PageMetadata(
    val description: String,
    val usage: String = "",
    val tags: Array<String> = [],
    val priority: Int = 0 // optional: for boosting relevance
)


open class PageRegistry : IPageRegistry {
    override lateinit var context: TestContext
}