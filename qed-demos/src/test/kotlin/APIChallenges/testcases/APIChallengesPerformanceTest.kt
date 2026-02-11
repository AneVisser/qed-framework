package APIChallenges.testcases

// SearchTest
import StressLevel
import PerformanceTestContext
import com.winterbe.expekt.expect
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import org.testng.annotations.Test
import qed.sut.apichallenges.APIChalURLPath
import qed.sut.apichallenges.SimEntities
import qed.testbaseclass.BaseTest
import qed.testbaseclass.HasRest
import qed.testbaseclass.LoggerContext
import withIOContext
import kotlin.random.Random
import qed.sut.apichallenges.Resp_SimEntities

private val baseTest = BaseTest()
private val hasRest = HasRest(baseTest, urlKey = "url")

private class APIChallengesPerformanceTest : PerformanceTestContext(baseTest, null, hasRest, stressLevel = StressLevel.BASELINE) {

    @Test(priority = 0, description = "Concurrent POST test with jitter", groups = ["All"])
    fun PostPerfTest() = runConcurrentTest("PostPerfTest") {

        logger.info{ "StressLevel: $stressLevel"}
        logger.info { "START TEST" }
        coroutineScope {
            val jobs = (0..9).map {
                launch(ctx) {
                    // delay adds some jitter to make simulation more realistic
                    delay(Random.nextLong(10, 100))
                    doPost(it)
                }
            }
            jobs.joinAll() // Wait for all coroutines to complete
        }
        logger.info { "END TEST" }
    }

    fun generatePayload(index: Int) =
         SimEntities("create todo #$index", index % 2 == 0, "description for task #$index")

    /**
     * coroutine executing the actual test
     */
    suspend fun doPost(index : Int) = withIOContext  {
        // jitter in ms
        delay(Random.nextLong(10, 100))
        val json = generatePayload(index)
        val result : Resp_SimEntities = rest.send(APIChalURLPath.POST_SIM_ENTITIES, json, listOf(201), trackPerformance = true)

        val logger = currentCoroutineContext()[LoggerContext]?.logger ?: error("LoggerContext missing")

        logger.info { "inside coroutine with POST #$index" }

        verify("check response body for name=bob and id=11") {
            expect(result.name).to.equal("bob")
            expect(result.id).to.equal(11)
        }
    }
}

