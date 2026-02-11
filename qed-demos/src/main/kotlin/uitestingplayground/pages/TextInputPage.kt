package qed.sut.uitestingplayground.pages

import qed.basepage.BasePage
import qed.testbaseclass.TestContext
import qed.uiwidgets.ComponentFactory.button
import qed.uiwidgets.ComponentFactory.inputfieldDelegate

class TextInputPage(context : TestContext) : BasePage(context) {

    // fields
    var textInput by inputfieldDelegate(context, "input[id='newButtonName']")
    val button = button(context, "button[id='updatingButton']")

}