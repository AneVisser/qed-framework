package qed.testbaseclass

import WidgetType
import qed.basepage.GenericPage
import qed.basepage.BasePage
import kotlin.time.Duration.Companion.seconds

enum class LoadStateSnapshot {
    LOADING,           // Document is still loading
    INTERACTIVE,       // DOM is parsed, sub-resources still loading
    COMPLETE           // Fully loaded
}

enum class BrowserName(val brwsr : String) {
    CHROMIUM("chromium"),
    FIREFOX("firefox"),
    WEBKIT("webkit")
}

open class HasBrowser(val baseTest : BaseTest, var urlKey : String) : IHasBrowser {

    private val browserDriver: ThreadLocal<BrowserDriver> = ThreadLocal.withInitial { BrowserDriver(baseTest.uniqueClassName, browserName) }

    override val browser : BrowserDriver
        get() = browserDriver.get()
    open lateinit var widgetType : WidgetType
    var browserName: BrowserName = BrowserName.CHROMIUM

    // this function needs to be overridden in the TestBaseClass for the specific application
    // to verify that you have loaded the following page.
    // this function verifies if the test has landed on the given application page.
    // each application can have its own implementation of how this is determined by implementing a
    // hasLandedOnPage function for that application. The default value is always true, but it can be made more sophisticated by
    // implementing a check on a unique field an an application page, for example a title.
    fun waitUntilLandedOnPage(appPage : BasePage) = retryUntil(5.seconds) {
        until { appPage.hasLandedOnPage() }
        onConditionNotMet {
            baseTest.logger.warn { "waiting for page timed out" }
            val media = browser.addScreenShot().media
            baseTest.extentTest.warning(media)
        }
    }

    fun removeBrowserDriver() = browserDriver.remove()

    // this function is used to start an application by navigating the landing page of a given url
    fun navigateToApp() {
        lateinit var targetURL : String
        baseTest.apply{targetURL = url(urlKey)}
        // if the targetURL starts with "ENV_", then it is a reference to an environment variable
        // that holds the url, so that the url doesn't need to be part of the repo
        if (targetURL.startsWith("ENV_")) {
            targetURL = System.getenv(targetURL)
        }
        browser.page.navigate(targetURL)
    }

    // navigation in the app is taken over by actions within the pages to move to the next.
    fun <T> startFromPage(landingPage : GenericPage<T>, block: T.() -> Unit) {
        baseTest.logger.info { "landing on ${landingPage.get()!!::class.toString().lastWord()}" }
        waitUntilLandedOnPage(landingPage.get() as BasePage)
        landingPage.get().block()
    }

    // this function returns the current load state of the page by querying JS readyState
    val loadState : LoadStateSnapshot
        get() {
            val state = browser.page.evaluate("document.readyState") as String
            return when (state.lowercase()) {
                "loading" -> LoadStateSnapshot.LOADING
                "interactive" -> LoadStateSnapshot.INTERACTIVE
                "complete" -> LoadStateSnapshot.COMPLETE
                else -> throw IllegalStateException("Unknown readyState: $state")
            }
        }

}






