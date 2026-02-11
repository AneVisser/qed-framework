package E2E_UITestingPlayground.testcases

// SearchTest
import com.winterbe.expekt.expect
import org.testng.annotations.Test
import qed.basepage.GenericPage
import qed.sut.uitestingplayground.pages.TextInputPage
import qed.sut.uitestingplayground.pages.UILandingPage
import qed.testbaseclass.*

private val baseTest = BaseTest()
private val hasBrowser = HasBrowser(baseTest, urlKey = "url")

private class E2E_UITestingPlayground4Test : TestContext(baseTest, hasBrowser) {

    @Test(priority=0, description = "fourth test of UI testing playground", groups = ["All"])
    fun fourthTest() {
        val landingPage = GenericPage(UILandingPage(this))
        val textInputPage = GenericPage(TextInputPage(this))

        hasBrowser?.apply {
            navigateToApp()
            startFromPage(landingPage) {
                textInput.click()
                onPage(textInputPage) {
                    textInput = "Ane Visser"
                    button.click()
                    verify("button text should be the same as text input ('${textInput}')") {
                        expect(textInput).to.equal(button.text)
                    }
                }
            }
        }
    }
/*
    @Test(priority=0, description = "fifth test of UI testing playground within same class")
    fun fifthTest() {
        val landingPage = GenericPage(UILandingPage(this))
        val textInputPage = GenericPage(TextInputPage(this))
        startApplication(landingPage) {
            textInput.click()
            onPage(textInputPage) {
                textInput.value = "Ane Visser"
                button.click()
                verify("button text should be the same as text input ('${textInput.value}')") {
                    expect(textInput.value).to.equal(button.text)
                }
            }
        }
    }

    @Test(priority=0, description = "sixth test of UI testing playground within same class")
    fun sixthTest() {
        val landingPage = GenericPage(UILandingPage(this))
        val textInputPage = GenericPage(TextInputPage(this))
        startApplication(landingPage) {
            textInput.click()
            onPage(textInputPage) {
                textInput.value = "Ane Visser"
                button.click()
                verify("button text should be the same as text input ('${textInput.value}')") {
                    expect(textInput.value).to.equal(button.text)
                }
            }

        }
    }
*/


}