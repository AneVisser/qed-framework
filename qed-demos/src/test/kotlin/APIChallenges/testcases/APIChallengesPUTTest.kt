package APIChallenges.testcases

// SearchTest
import com.winterbe.expekt.expect
import qed.testbaseclass.BaseTest
import qed.testbaseclass.HasRest
import qed.testbaseclass.TestContext
import qed.sut.apichallenges.APIChalURLPath
import org.testng.annotations.Test
import qed.json.get
import qed.sut.apichallenges.SimEntities

private val baseTest = BaseTest()
private val hasRest = HasRest(baseTest, urlKey = "url")

private class APIChallengesPUTTest : TestContext(baseTest, null, hasRest) {

    @Test(priority = 0, description = "third test of API testing playground, POST", groups = ["All"])
    fun PutTest() {
        val simEntities = SimEntities("create todo process payroll", true, "description")
        var result = rest.sendUntyped(APIChalURLPath.PUT_SIM_ENTITIES, pathParams = mapOf("entity" to "10"), trackPerformance = true)
        logger.info { result }
        verify("check response body") {
            expect(result.get("name")).to.equal("eris")
            expect(result.get("id")).to.equal(10)
        }
        result = rest.sendUntyped(APIChalURLPath.PUT_SIM_ENTITIES, simEntities, pathParams = mapOf("entity" to "10"), trackPerformance = true)
        verify("check response body") {
            expect(result.get("name")).to.equal("eris")
            expect(result.get("description")).to.equal("")      // the api doesn't change the value of description
        }
    }


}