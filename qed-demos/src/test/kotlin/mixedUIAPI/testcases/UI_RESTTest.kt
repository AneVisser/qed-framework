package mixedUIAPI.testcases

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.winterbe.expekt.expect
import org.testng.annotations.Test
import qed.basepage.GenericPage
import qed.sut.apichallenges.APIChalURLPath
import qed.sut.apichallenges.SimEntities
import qed.sut.mixedUIAPI.pages.UITestingPage
import qed.testbaseclass.*
import qed.json.get

private val baseTest = BaseTest()
private val hasRest = HasRest(baseTest, urlKey = "apichallenges")
private val hasBrowser = HasBrowser(baseTest, urlKey = "uitestingurl")


class UI_RESTTest : TestContext(baseTest, hasBrowser, hasRest) {

    inline fun <reified T> Moshi.jsonAdapter(): JsonAdapter<T> =
        this.adapter(T::class.java)



    @Test(priority=0, description = "mixed UI/API test", groups = ["All"])
    fun testUI_Rest() {
        val payroll = SimEntities("create todo process payroll", true, "todo list needs to be added")
        logger.info { "opening UI Testing Playground" }
        val landingPage = GenericPage(UITestingPage(this))
        hasBrowser?.apply {
            navigateToApp()
            startFromPage(landingPage) {
                val result = rest.sendUntyped(APIChalURLPath.POST_SIM_ENTITIES, payroll, listOf(201))
                logger.info { "API test within ui test" }
                logger.info { result }
                verify("check response body") {
                    expect(result.get("name")).to.equal("bob")
                    expect(result.get("id")).to.equal(11)
                }
            }
        }
    }
    
}

