package mixedUIAPI.testcases

import org.testng.annotations.Test
import qed.basepage.GenericPage
import qed.sut.mixedUIAPI.pages.UITestingPage
import qed.testbaseclass.*


private val baseTest = BaseTest()
private val hasBrowser = HasBrowser(baseTest, urlKey = "uitestingurl")

class LoginUI_2Test : TestContext(baseTest, hasBrowser) {

    @Test(priority=0, description = "UI test for UI login (UITestingPlayground))", groups = ["All"])
    fun testUITestingPlayground() {
        logger.info { "opening UITestingPlayground" }
        val landingPage = GenericPage(UITestingPage(this))
        hasBrowser?.apply {
            navigateToApp()
            startFromPage(landingPage) {
            }
        }
    }


}

