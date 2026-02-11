package APIChallenges.testcases

// SearchTest
import com.winterbe.expekt.expect
import qed.testbaseclass.BaseTest
import qed.testbaseclass.HasRest
import qed.testbaseclass.TestContext
import qed.sut.apichallenges.APIChalURLPath
import org.testng.annotations.Test
import qed.json.get

private val baseTest = BaseTest()
private val hasRest = HasRest(baseTest, urlKey = "url")

private class APIChallengesGETTest : TestContext(baseTest, null, hasRest) {

    @Test(priority = 0, description = "first test of API testing playground, GET", groups = ["All"])
    fun GetTest() {
        var result = rest.sendUntyped(APIChalURLPath.GET_SIM_ENTITIES, pathParams = mapOf("entity" to "1"), trackPerformance = true)
        logger.info { result }
        verify("check response body for id=1") {
            expect(result.get("name")).to.equal("entity number 1")
            expect(result.get("id")).to.equal(1)
        }

        result = rest.sendUntyped(APIChalURLPath.GET_SIM_ENTITIES, pathParams = mapOf("entity" to "10"), trackPerformance = true)
        logger.info { result }
        verify("check response body for id=10") {
            expect(result.get("name")).to.equal("eris")
            expect(result.get("id")).to.equal(10)
        }

        result = rest.sendUntyped(APIChalURLPath.TODOS, trackPerformance = true)
        logger.info { result }
        verify("check response body for id=10") {
//            expect(result.get("todos")).to.equal("eris")
        }

    }


}