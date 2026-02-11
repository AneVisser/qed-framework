package qed.uiwidgets

import qed.testbaseclass.TestContext

class ClickableWidget(context : TestContext, selector : String) : Clickable(context), IAtomicWidget by AtomicWidget(
    context,
    selector
) {

    override fun click() {
//        pageElement.scrollIntoViewIfNeeded()
//        pageElement.waitFor()
        pageElement.click()
    }

}