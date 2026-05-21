package qed.uiwidgets.widgetlibs.base

import com.microsoft.playwright.Locator

interface ITable {

    val headerLocators: List<Locator>

    fun getColumnIndex(headerText: String): Int?

    fun cell(rowIndex: Int, columnIndex: Int): Locator

    fun cellSetValue(rowIndex: Int, columnIndex: Int, value : String)

    fun columnValues(columnIndex: Int): List<String>

    fun row(rowIndex: Int) : Locator

    fun allRows(): List<Locator>

    fun rowValues(rowIndex: Int) : List<String>

    fun rowsWith(constraints: List<Pair<Int, String>>) : List<Int>

    // to support the following syntax in validations: expect(table[1, 2].textContent()).to.equal("Price")
    operator fun get(rowIndex: Int, columnIndex: Int): Locator

    fun locatorInRow(rowIndex: Int, selector: String): Locator
}