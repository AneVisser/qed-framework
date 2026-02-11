package qed.basepage

import com.microsoft.playwright.Dialog
import com.winterbe.expekt.expect

enum class DialogType(val apply: (String, Dialog) -> Unit) {
    ALERT({ msg, dialog -> (expect(dialog.type()).to.equal("alert")) }),
    CONFIRM({ msg, dialog -> (expect(dialog.type()).to.equal("confirm")) }),
    PROMPT({ msg, dialog -> (expect(dialog.type()).to.equal("prompt")) })
}

