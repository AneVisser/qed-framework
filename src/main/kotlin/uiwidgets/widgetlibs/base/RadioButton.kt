package qed.uiwidgets.widgetlibs.base

import qed.testbaseclass.TestContext
import qed.uiwidgets.ClickableWidget
import qed.uiwidgets.IClickable

/**
 * Todo: add some validation to the selectors, so that an input field always contains 'input', a button always 'button' etc
 */

// todo: return value whether or not checked
class RadioButton(context : TestContext, selector : String)  : IClickable by ClickableWidget(context, selector) {

}

