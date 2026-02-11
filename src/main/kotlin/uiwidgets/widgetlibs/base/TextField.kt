package qed.uiwidgets.widgetlibs.base

import qed.testbaseclass.TestContext
import qed.uiwidgets.AtomicWidget
import kotlin.reflect.KProperty

/**
 * Todo: add some validation to the selectors, so that an input field always contains 'input', a button always 'button' etc
 */

class TextField(context : TestContext, selector : String)  : AtomicWidget(context, selector)  {
    val value : String
        get() = pageElement.innerText()
}

class TextFieldDelegate(
    private val testContext: TestContext,
    private val selector: String
) {
    private val textField = TextField(testContext, selector)

    operator fun getValue(thisRef: Any?, property: KProperty<*>): String {
        return textField.value
    }
}
