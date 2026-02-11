import kotlinx.coroutines.*
import qed.testbaseclass.*
import kotlin.coroutines.CoroutineContext

abstract class PerformanceTestContext(
    base: BaseTest,
    browser: HasBrowser?,
    rest: HasRest?,
    val stressLevel: StressLevel = StressLevel.BASELINE
) : TestContext(base, browser, rest) {

    lateinit var ctx : CoroutineContext

    fun runConcurrentTest(
        name: String,
        modeContext: CoroutineContext? = null,  // optional
        block: suspend CoroutineScope.() -> Unit
    ) = runBlocking(LoggerContext(logger)) {
        ctx = modeContext ?: when(stressLevel) {
            StressLevel.BASELINE -> Dispatchers.IO + coroutineContext
            StressLevel.STRESS   -> Dispatchers.IO
        }
        logger.info { "<b>" + "Starting performance test : ".uppercase() +  "$name</b>" }
        coroutineScope {
            block()
        }
        logger.info { "<b>" + "Finished performance test : ".uppercase() +  "$name</b>" }
    }

}


suspend fun <T> withIOContext(block: suspend () -> T): T =
    withContext(Dispatchers.IO + currentCoroutineContext()) { block() }

enum class StressLevel { BASELINE, STRESS }