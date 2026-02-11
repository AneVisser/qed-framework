import qed.basepage.DialogType
import qed.basepage.ExpectedDialog
import qed.basepage.ShouldNotAccessHigherContainer

/**
 * this is a DSL specifically for dialogs, and therefore should not contain other function calls
 * outside the dialog builder
 */

@ShouldNotAccessHigherContainer
class DialogDslBuilder {
    private val dialogs = mutableListOf<ExpectedDialog>()

    fun build(): List<ExpectedDialog> = dialogs

    fun prompt(message: String, block: DialogConfig.() -> Unit) {
        val config = DialogConfig(DialogType.PROMPT, message).apply(block)
        dialogs += config.toExpectedDialog()
    }

    fun alert(message: String, block: DialogConfig.() -> Unit) {
        val config = DialogConfig(DialogType.ALERT, message).apply(block)
        dialogs += config.toExpectedDialog()
    }

    fun confirm(message: String, block: DialogConfig.() -> Unit) {
        val config = DialogConfig(DialogType.CONFIRM, message).apply(block)
        dialogs += config.toExpectedDialog()
    }

    class DialogConfig(val type: DialogType, val message: String) {
        private var input: String? = null
        private var accepted: Boolean = true

        fun enter(value: String) {
            input = value
        }

        fun accept() {
            accepted = true
        }

        fun dismiss() {
            accepted = false
        }

        fun toExpectedDialog(): ExpectedDialog = ExpectedDialog(
            type = type,
            expMessage = message,
            enter = input,
            accept = accepted
        )
    }
}
