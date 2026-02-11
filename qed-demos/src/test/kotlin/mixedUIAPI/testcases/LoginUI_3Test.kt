package mixedUIAPI.testcases

import org.testng.annotations.Test
import qed.basepage.GenericPage
import qed.sut.mixedUIAPI.pages.PBTechPage
import qed.testbaseclass.*

private val baseTest = BaseTest()
private val hasBrowser = HasBrowser(baseTest, urlKey = "pbtechurl")

class LoginUI_3Test : TestContext(baseTest, hasBrowser) {


    @Test(priority=0, description = "UI test for UI login (PBTech))", groups = ["All"])
    fun testPBTech() {
        logger.info { "opening PBTech" }
        val landingPage = GenericPage(PBTechPage(this))
        hasBrowser?.apply {
            navigateToApp()
            startFromPage(landingPage) {
            }
        }
    }
    
}

