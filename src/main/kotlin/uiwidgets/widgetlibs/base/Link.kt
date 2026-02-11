package qed.uiwidgets.widgetlibs.base

import qed.testbaseclass.TestContext
import qed.uiwidgets.ClickableWidget
import qed.uiwidgets.IClickable
import kotlin.reflect.KProperty

/**
 * Todo: add some validation to the selectors, so that an input field always contains 'input', a button always 'button' etc
 */


class Link(context : TestContext, selector : String) : IClickable by ClickableWidget(context, selector) {

    val value
        get() = pageElement.innerText()

}

class LinkDelegate(
    private val testContext: TestContext,
    private val selector: String
) {
    private val linkField = Link(testContext, selector)

    operator fun getValue(thisRef: Any?, property: KProperty<*>): String {
        return linkField.value
    }

}