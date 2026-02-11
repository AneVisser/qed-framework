package qed.uiwidgets.widgetlibs.base

import qed.testbaseclass.LoadStateSnapshot
import qed.testbaseclass.TestContext
import qed.testbaseclass.retryUntil
import qed.uiwidgets.AtomicWidget
import kotlin.reflect.KProperty

/**
 * Todo: add some validation to the selectors, so that an input field always contains 'input', a button always 'button' etc
 */


class InputField(context : TestContext, selector : String) : AtomicWidget(context, selector) {
    private val ifield = this
    var value : String
        get() = pageElement.inputValue()
        set(value) {
            // don't proceed until you know that the value has been set correctly
            retryUntil {
                action {
                    pageElement.fill("")
                    pageElement.fill(value)
                    pageElement.press("Tab")
                }
                until {
                    ifield.value == value
                }
                onConditionNotMet {
                    println("input value not set: ${it.message}")
                }
            }
            retryUntil {
                until {
                    context.hasBrowser?.loadState == LoadStateSnapshot.COMPLETE
                }
            }
        }
}

class InputFieldDelegate(
    private val testContext: TestContext,
    private val selector: String
) {
    private val inputField = InputField(testContext, selector)

    operator fun getValue(thisRef: Any?, property: KProperty<*>): String {
        return inputField.value
    }

    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: String) {
        inputField.value = value
    }
}

