package E2E_UITestingPlayground.testcases

// SearchTest
import org.testng.Assert
import org.testng.annotations.Parameters
import org.testng.annotations.Test
import qed.basepage.GenericPage
import qed.sut.uitestingplayground.pages.UILandingPage
import qed.testbaseclass.*

private val baseTest = BaseTest()
private val hasBrowser = HasBrowser(baseTest, urlKey = "url")

private class E2E_UITestingPlayground1Test : TestContext(baseTest, hasBrowser) {
    @Parameters("par1", "par2")

    @Test(priority=0, description = "first test of UI testing playground (expected fail)", groups = ["All"])
    fun firstTest() {
        val landingPage = GenericPage(UILandingPage(this))
        logger.trace { "This is trace log" }
        logger.debug { "This is debug log" }
        logger.info { "This is info log" }
        logger.warn { "This is warn log" }
        logger.error { "This is error log" }
        hasBrowser?.apply {
            navigateToApp()
            startFromPage(landingPage) {
                dynamicID.click()
            }
        }
        Assert.assertEquals("ABC", "ABC")  // dummy assertion
    }


}