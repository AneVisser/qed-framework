package mixedUIAPI.testcases

import com.winterbe.expekt.expect
import qed.testbaseclass.BaseTest
import qed.testbaseclass.HasRest
import qed.testbaseclass.TestContext
import qed.sut.apichallenges.APIChalURLPath
import org.testng.annotations.Test
import qed.sut.apichallenges.SimEntities
import qed.json.get

private val baseTest = BaseTest()
private val hasRest = HasRest(baseTest, urlKey = "apichallenges")

class LoginRestTest : TestContext(baseTest, null, hasRest) {

    @Test(priority=0, description = "REST test for API login", groups = ["All"])
    fun testLoginApi() {
        val simEntities = SimEntities("create todo process payroll", true, "description")
        val result = rest.sendUntyped(APIChalURLPath.POST_SIM_ENTITIES, simEntities, listOf(201))
        logger.info { result }
        verify("check response body") {
            expect(result.get("name")).to.equal("bob")
            expect(result.get("id")).to.equal(11)
        }
    }
}

