package qed.sut.mixedUIAPI.pages
// SearchPage
// Let's describe locators and actions that we can perform with this page

import qed.basepage.BasePage
import qed.testbaseclass.TestContext
import qed.uiwidgets.ComponentFactory.link
import qed.uiwidgets.widgetlibs.base.Link

class UITestingPage(context : TestContext) : BasePage(context) {

// fields
    val dynamicID = link(context, "a[href='/dynamicid']")
    val AJAXData = link(context,"a[href='/ajax']")
    val ScrollBars = link(context, "a[href='/scrollbars']")
    val textInput = link(context,"a[href='/textinput']")
    val alerts = link(context,"a[href='/alerts']")
}