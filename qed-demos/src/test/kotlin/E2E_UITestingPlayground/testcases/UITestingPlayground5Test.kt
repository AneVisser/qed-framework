package E2E_UITestingPlayground.testcases

// SearchTest
import org.testng.annotations.Parameters
import org.testng.annotations.Test
import qed.basepage.GenericPage
import qed.sut.uitestingplayground.pages.AlertsPage
import qed.sut.uitestingplayground.pages.UILandingPage
import qed.testbaseclass.*

private val baseTest = BaseTest()
private val hasBrowser = HasBrowser(baseTest, urlKey = "url")

private class E2E_UITestingPlayground5Test : TestContext(baseTest, hasBrowser) {
    @Parameters("par1", "par2")

    @Test(priority=0, description = "UI testing playground Dialogs", groups = ["All"])
    fun fifthTest() {
        logger.info { "handling dialogs" }

        val landingPage = GenericPage(UILandingPage(this))
        val alertsPage = GenericPage(AlertsPage(this))
        hasBrowser?.apply {
            navigateToApp()
            startFromPage(landingPage) {
                alerts.click()
                onPage(alertsPage) {
                    alertButton.handleDialog {
                        alert( "Today is a working day.\nOr less likely a holiday.") {
                            accept()
                        }
                    }
                    confirmButton.handleDialog {
                        confirm("Today is Friday.\nDo you agree?") {
                            accept()
                        }
                        alert("Yes") {
                            accept()
                        }
                    }
                    confirmButton.handleDialog {
                        confirm("Today is Friday.\nDo you agree?") {
                            dismiss()
                        }
                        alert("No") {
                            accept()
                        }
                    }

                    promptButton.handleDialog {
                        prompt("Choose \"cats\" or 'dogs'.\nEnter your value:") {
                            enter("bla bla")
                            accept()
                        }
                        alert("User value: bla bla") {
                            accept()
                        }
                    }
                }
            }
        }
    }

}

