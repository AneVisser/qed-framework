package qed.uiwidgets

import com.microsoft.playwright.Locator

interface IAtomicWidget {
    val pageElement : Locator
    val isVisible : Boolean
    val exists : Boolean
}