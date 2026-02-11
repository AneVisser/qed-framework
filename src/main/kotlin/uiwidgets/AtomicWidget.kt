package qed.uiwidgets

import com.microsoft.playwright.Locator
import qed.testbaseclass.TestContext
import qed.testbaseclass.retryUntil
import kotlin.time.Duration.Companion.seconds

/**
 * Todo: add some validation to the selectors, so that an input field always contains 'input', a button always 'button' etc
 */

open class AtomicWidget(val context : TestContext, val selector : String, val visible : String = "true") :
    IAtomicWidget {

    val widget = this
    override val pageElement : Locator
        get() {
            retryUntil(5.seconds) {
                until { widget.context.browser.page.locator(selector)!!.all().isNotEmpty()}
            }
            return context.browser.page.locator(selector)!!.locator("visible=$visible")
        }

    override val isVisible : Boolean
        get() = pageElement.isVisible()
}

