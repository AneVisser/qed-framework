package qed.uiwidgets

import DialogDslBuilder
import qed.basepage.DialogHandler
import qed.testbaseclass.TestContext

abstract class Clickable(val context : TestContext) : IClickable {

    // function click not implemented here, as they are implemented in the different derivative classes
    // hence this being an abstract class

    override fun handleDialog(build: DialogDslBuilder.() -> Unit) {
        val builder = DialogDslBuilder().apply(build)
        val expectedDialogs = builder.build()
        DialogHandler(context, expectedDialogs).clickAndHandleDialog(
            trigger = this,
        )
    }

}

