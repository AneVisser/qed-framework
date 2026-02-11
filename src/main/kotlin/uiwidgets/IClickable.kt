package qed.uiwidgets

import DialogDslBuilder

interface IClickable : IAtomicWidget {
    fun click()

    fun handleDialog(build: DialogDslBuilder.() -> Unit)

}