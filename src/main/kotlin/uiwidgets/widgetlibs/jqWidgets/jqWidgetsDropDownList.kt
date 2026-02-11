package qed.uiwidgets.widgetlibs.jqWidgets

import qed.testbaseclass.TestContext
import qed.testbaseclass.retryUntil
import qed.uiwidgets.AtomicWidget
import qed.uiwidgets.IPickValue
import qed.uiwidgets.widgetlibs.base.BaseDropDownListDelegate
import qed.uiwidgets.widgetlibs.base.IDropDownList
import kotlin.enums.EnumEntries
import kotlin.time.Duration.Companion.seconds

/**
 * Todo: add some validation to the selectors, so that an input field always contains 'input', a button always 'button' etc
 */

class jqWidgetsDropDownList<T>(context : TestContext, selector : String, val entriesList: EnumEntries<T>)  : IDropDownList<T>, AtomicWidget(context, selector) where T:Enum<T>, T : IPickValue {

    override var value : T
        get() {
            val selectedValue = pageElement.innerText()
            return entriesList.first{it.PickValue == selectedValue}
        }
        set(value) {
            val id = pageElement.getAttribute("id")
            pageElement.click()
            // looks like all available options are stored in a div with an id that has the prefix 'innerListBox', followed by the fields' id
            val options = context.hasBrowser?.browser?.page?.locator("div[id='innerListBox$id'] div[role='option']")
            val count = options?.count() ?: 0
            for (i in 0 until count) {
                val handle = options?.nth(i)?.elementHandle()
                if (handle?.innerText() == value.PickValue) {
                    //todo: not clear whatthe condition is that we are waiting for. Needs to be found to replace Thread.sleep.
                    Thread.sleep(500)
                    handle.click()
                    break
                }
            }
            // verify that the value has been selected correctly within 1 second
            retryUntil(1.seconds) {
                until {
                    pageElement.innerText() == value.PickValue
                }
                onConditionNotMet {
                    throw Exception( "dropdown box value not set to ${value.PickValue} within 1 second" )
                }
            }
        }

}

class jqWidgetsDropDownListDelegate<T>(
    private val owner: TestContext,
    private val selector: String,
    private val entriesList: EnumEntries<T>
) : BaseDropDownListDelegate<T, jqWidgetsDropDownList<T>>(owner, selector, entriesList) where T : Enum<T>, T : IPickValue {

    override fun createDropDownList(
        owner: TestContext,
        selector: String,
        entriesList: EnumEntries<T>
    ): jqWidgetsDropDownList<T> {
        return jqWidgetsDropDownList(owner, selector, entriesList)
    }
}
