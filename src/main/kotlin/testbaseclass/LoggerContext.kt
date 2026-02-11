package qed.testbaseclass

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.withContext
import qed.reports.Logger
import kotlin.coroutines.AbstractCoroutineContextElement
import kotlin.coroutines.CoroutineContext

/**
 * This class allows to use a logger accross coroutines within a test run. This is mainly used in
 * performance testing. This way, the syntax for logger.info {..} etc remains intact and can be
 * used within co-routines.
 *
 * how to set it up:
 *
 *  private class APIChallengesPerformanceTest :  TestContext(baseTest, null, hasRest) {
 *     val loggerContext = LoggerContext(logger)   //<<--- important
 *
 *     @Test(priority = 0, description = "Concurrent POST test with jitter", groups = ["All"])
 *     fun Post1Test() = runBlocking(loggerContext) {    // <<-- loggerContext used here
 *
 *         logger.info { "START TEST" }
 *         val jobs = (0..9).map {
 *             launch {
 *                 doPost(it)                           // <<-- call to coroutine that executes the test
 *             }
 *         }
 *         jobs.joinAll()                               // <<-- Wait for all coroutines to complete
 *         logger.info { "END TEST" }
 *     }
 *
 * Then the definition of the coroutine performing the actual test:
 *
 *     suspend fun doPost(index : Int) = withIOContext  {       // <<-- note th euse of withIOContent
 *         val json = generatePayload(index)
 *         val result = rest.send(RequestType.POST, APIChalURLPath.SIM_ENTITIES, json, 201)
 *
 *         val logger = currentCoroutineContext()[LoggerContext]?.logger ?: error("LoggerContext missing")      // make logger couroutine-aware
 *
 *         logger.info { "inside coroutine with POST #$index" }
 *
 *         verify("check response body for name=bob and id=11") {       // <<-- note that verify internally now also uses the coroutine-aware logger
 *             expect(result.get("name")).to.equal("bob")
 *             expect(result.get("id")).to.equal(11)
 *         }
 *     }
 *
 */
class LoggerContext(val logger: Logger) : AbstractCoroutineContextElement(Key) {
    companion object Key : CoroutineContext.Key<LoggerContext>
}

