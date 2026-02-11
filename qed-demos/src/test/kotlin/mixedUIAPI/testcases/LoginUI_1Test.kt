package mixedUIAPI.testcases

import org.testng.annotations.Test
import qed.basepage.GenericPage
import qed.sut.mixedUIAPI.pages.GooglePage
import qed.testbaseclass.*

private val baseTest = BaseTest()
private val hasBrowser = HasBrowser(baseTest, urlKey = "googleurl")

class LoginUI_1Test : TestContext(baseTest, hasBrowser) {

    @Test(priority=0, description = "UI test for UI login (google.com)", groups = ["All"])
    fun testGoogle() {
        logger.info { "opening Google" }
        val landingPage = GenericPage(GooglePage(this))
        hasBrowser?.apply {
            navigateToApp()
            startFromPage(landingPage) {
            }
        }
    }

}
