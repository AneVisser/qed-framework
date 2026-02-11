package E2E_UITestingPlayground.testcases

// SearchTest
import org.testng.Assert
import org.testng.annotations.Test
import qed.basepage.GenericPage
import qed.sut.uitestingplayground.pages.UILandingPage
import qed.testbaseclass.*

private val baseTest = BaseTest()
private val hasBrowser = HasBrowser(baseTest, urlKey = "url")

private class E2E_UITestingPlayground3Test : TestContext(baseTest, hasBrowser) {

    @Test(priority=0, description = "third test of UI testing playground (expected fail)", groups = ["All"])
    fun thirdTest() {
        val landingPage = GenericPage(UILandingPage(this))

        hasBrowser?.apply {
            navigateToApp()
            startFromPage(landingPage){
                AJAXData.click()
            }
        }
        Assert.assertEquals("Abc", "ABC")  // dummy assertion
    }

}