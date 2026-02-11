package qed.basepage

import com.microsoft.playwright.Dialog
import com.microsoft.playwright.PlaywrightException
import com.winterbe.expekt.expect
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import qed.testbaseclass.TestContext
import qed.uiwidgets.IClickable
import kotlin.properties.Delegates

// handler for dialogs. Playwright by design does not dispatch a trusted event when a clickable element triggers
// a dialog, if that would have been a true manual click on the element. With page.onceDialog, a trusted event is generated, so that
// handling the dialog can proceed. This function is used in the BasePage to handle the various browser dialog options
// There are some timing issues with the dialoghandler in Playwright. It is attempted to solve that by creating a handler for each
// expected dialog, and then discarding the dialog handler while it isbeing used by page?.offDialog{this}

data class ExpectedDialog(
    val type: DialogType,
    val expMessage: String,
    val enter : String? = null,
    val accept : Boolean = true
)


class DialogHandler(val context: TestContext, private val expected: List<ExpectedDialog>) {

    var dialogCount by Delegates.notNull<Int>()
    init {
        dialogCount = 0
    }

    fun verifyDialogType(dialogType : DialogType, message : String) {
        context.verify("verify that current dialog is ${expected[dialogCount].type.name.lowercase()} and message is: '$message'") {
            expect(dialogType).to.equal(expected[dialogCount].type)
            expect(message).to.equal(expected[dialogCount].expMessage)
        }
    }

    fun dialogHandler(dialog : Dialog,
                        accept : Boolean
    ) {
        val msg = dialog.message()
        var alreadyHandled  = false
        val currentDialogType = DialogType.entries.first { it.name.lowercase()== dialog.type()}
        when (currentDialogType) {
            DialogType.ALERT -> {
                try {
                    dialog.accept()
                    context.logger.info { "Alert says: $msg" }
                    verifyDialogType(currentDialogType, msg)
                    dialogCount++
                } catch (e: PlaywrightException) {
                    alreadyHandled = true
                }
            }

            DialogType.CONFIRM -> {
                try {
                    if (accept)
                        dialog.accept()
                    else
                        dialog.dismiss()
                    context.logger.info { "Confirm says: $msg; acepted=$accept" }
                    verifyDialogType(currentDialogType, msg)
                    dialogCount++
                } catch (e: PlaywrightException) {
                    alreadyHandled = true
                }
            }
            DialogType.PROMPT ->  {
                val input = expected[dialogCount].enter
                try {
                    if (accept) {
                        if (input != null)
                            dialog.accept(input)
                        else
                            dialog.accept()
                    }
                    else
                        dialog.dismiss()
                    context.logger.info { "Prompt says: $msg; input=$input; accepted=$accept" }
                    verifyDialogType(currentDialogType, msg)
                    dialogCount++
                } catch (e: PlaywrightException) {
                    alreadyHandled = true
                }
            }
        }
        // discard current instance of handler, for timing purposes
        context.browser.page.offDialog { this }
        // it is not totally clear what the condition is that we are waiting for here
        // without it, some alerts are not reported.
        // todo: determine whatthe condition is we are waiting for to minimise the time required to handle the dialog
        runBlocking {
            launch {
                if (!alreadyHandled) {
                    delay(1000L) // non-blocking delay for 1 second (default time unit is ms)
                }
            }
        }
    }

    fun clickAndHandleDialog(
        trigger: IClickable,
    ) {
        val startTime = System.currentTimeMillis()
        expected.forEach { exp ->
            context.browser.page.onceDialog{ dialog -> dialogHandler(dialog, exp.accept) }
        }
        trigger.click()
        val elapsed = System.currentTimeMillis() - startTime
        if (elapsed > 1500 * expected.count()) {
            context.logger.warn { "Dialog appeared after ${elapsed}ms — possible UI slowness." }
        }
    }

}

