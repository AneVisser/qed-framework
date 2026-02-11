package qed.sut.uitestingplayground.pages

import qed.basepage.BasePage
import qed.testbaseclass.TestContext
import qed.uiwidgets.ComponentFactory.button
import qed.uiwidgets.widgetlibs.base.Button

class AlertsPage(context : TestContext) : BasePage(context) {


    // fields
    val alertButton = button(context, "button[id='alertButton']")
    val confirmButton = button(context, "button[id='confirmButton']")
    val promptButton = button(context, "button[id='promptButton']")



}

