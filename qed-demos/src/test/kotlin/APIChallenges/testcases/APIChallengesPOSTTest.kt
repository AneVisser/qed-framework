package APIChallenges.testcases

// SearchTest
import com.winterbe.expekt.expect
import qed.testbaseclass.BaseTest
import qed.testbaseclass.HasRest
import qed.testbaseclass.TestContext
import qed.sut.apichallenges.APIChalURLPath
import org.testng.annotations.Test
import qed.sut.apichallenges.Resp_SimEntities
import qed.sut.apichallenges.SimEntities

private val baseTest = BaseTest()
private val hasRest = HasRest(baseTest, urlKey = "url")

private class APIChallengesPOSTTest :  TestContext(baseTest, null, hasRest) {

@Test(priority = 0, description = "second test of API testing playground, POST", groups = ["All"])
    fun PostTest() {
        val simEntities = SimEntities("create todo process payroll", true, "description")
        val result : Resp_SimEntities = rest.send(APIChalURLPath.POST_SIM_ENTITIES, simEntities, listOf(201), trackPerformance = true)
        logger.info { result }
        verify("check response body") {
            expect((result).name).to.equal("bob")
            expect((result).id).to.equal(11)
        }
    }


}