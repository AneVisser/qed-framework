package qed.uiwidgets.widgetlibs.base

import qed.testbaseclass.TestContext
import qed.testbaseclass.retryUntil
import qed.uiwidgets.AtomicWidget
import kotlin.reflect.KProperty

class DynamicDropDownList(
    context: TestContext,
    selector: String
) : IDynamicDropdownList, AtomicWidget(context, selector) {

    private val ddlist = this

    override var value: String
        get() = pageElement.evaluate("sel => sel.options[sel.options.selectedIndex].textContent") as String
        set(value) {
            retryUntil {
                action {
                    pageElement.selectOption(value)
                    pageElement.press("Tab")
                }
                until {
                    ddlist.value == value
                }
                onConditionNotMet {
                    println("Condition not met: ${it.message}")
                }
            }
        }
}


// Delegate for dynamic dropdowns
class DynamicDropDownListDelegate(
    private val owner: TestContext,
    private val selector: String
) : BaseDynamicDropDownListDelegate<DynamicDropDownList>(owner, selector) {

    override fun createDynamicDropDownList(
        owner: TestContext,
        selector: String
    ): DynamicDropDownList {
        return DynamicDropDownList(owner, selector)
    }
}