package qed.uiwidgets.widgetlibs.base

import qed.testbaseclass.LoadStateSnapshot
import qed.testbaseclass.TestContext
import qed.testbaseclass.retryUntil
import qed.uiwidgets.AtomicWidget
import qed.uiwidgets.IPickValue
import kotlin.enums.EnumEntries

/**
 * Todo: add some validation to the selectors, so that an input field always contains 'input', a button always 'button' etc
 */

class DropDownList<T>(context: TestContext, selector: String, val entriesList: EnumEntries<T>)  : IDropDownList<T>, AtomicWidget(context, selector) where T:Enum<T>, T : IPickValue {

    private val ddlist = this
//    private val dynamicValues = mutableSetOf<String>()

    override var value : T
        get() {
            val selectedValue = pageElement.evaluate("sel => sel.options[sel.options.selectedIndex].textContent")
            return entriesList.first{it.PickValue == selectedValue}
        }
        set(value) {
            val option = entriesList.find{it == value }!!
            retryUntil {
                action {
                    pageElement.selectOption(option.PickValue)
                    pageElement.press("Tab")
                }
                until {
                    ddlist.value==value
                }
                onConditionNotMet {
                    println("Condition not met: ${it.message}")
                }
            }
            retryUntil {
                until {
                    context.hasBrowser?.loadState == LoadStateSnapshot.COMPLETE
                }
            }
        }
}

class DropDownListDelegate<T>(
    private val owner: TestContext,
    private val selector: String,
    private val entriesList: EnumEntries<T>
) : BaseDropDownListDelegate<T, DropDownList<T>>(owner, selector, entriesList) where T : Enum<T>, T : IPickValue {

    override fun createDropDownList(
        owner: TestContext,
        selector: String,
        entriesList: EnumEntries<T>
    ): DropDownList<T> {
        return DropDownList(owner, selector, entriesList)
    }

}
