package qed.uiwidgets.widgetlibs.base

import qed.testbaseclass.TestContext
import qed.uiwidgets.AtomicWidget

/**
 * Todo: add some validation to the selectors, so that an input field always contains 'input', a button always 'button' etc
 */

class HoverArea(context : TestContext, selector : String)  : AtomicWidget(context, selector)  {
    fun hover() = pageElement.hover()
}
