package qed.uiwidgets.widgetlibs.jqWidgets

import qed.testbaseclass.TestContext
import qed.testbaseclass.retryUntil
import qed.uiwidgets.AtomicWidget
import qed.uiwidgets.widgetlibs.base.BaseDynamicDropDownListDelegate
import qed.uiwidgets.widgetlibs.base.DynamicDropDownList
import qed.uiwidgets.widgetlibs.base.IDynamicDropdownList
import kotlin.time.Duration.Companion.seconds

class jqWidgetsDynamicDropdownList(
    context: TestContext,
    selector: String
) : IDynamicDropdownList, AtomicWidget(context, selector){
    override var value: String
    get() = pageElement.innerText()
    set(value) {
        val id = pageElement.getAttribute("id")
        pageElement.click()
        // looks like all available options are stored in a div with an id that has the prefix 'innerListBox', followed by the fields' id
        val options = context.hasBrowser?.browser?.page?.locator("div[id='innerListBox$id'] div[role='option']")
        val count = options?.count() ?: 0
        for (i in 0 until count) {
            val handle = options?.nth(i)?.elementHandle()
            if (handle?.innerText() == value) {
                //todo: not clear what the condition is that we are waiting for. Needs to be found to replace Thread.sleep.
                Thread.sleep(500)
                handle.click()
                break
            }
        }
        // verify that the value has been selected correctly within 1 second
        retryUntil(1.seconds) {
            until {
                pageElement.innerText() == value
            }
            onConditionNotMet {
                throw Exception( "dropdown box value not set to ${value} within 1 second" )
            }
        }
    }
}

// Delegate for dynamic dropdowns
class jqWidgetsDynamicDropDownListDelegate(
    private val owner: TestContext,
    private val selector: String
) : BaseDynamicDropDownListDelegate<jqWidgetsDynamicDropdownList>(owner, selector) {

    override fun createDynamicDropDownList(
        owner: TestContext,
        selector: String
    ): jqWidgetsDynamicDropdownList {
        return jqWidgetsDynamicDropdownList(owner, selector)
    }
}