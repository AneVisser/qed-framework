package qed.uiwidgets.widgetlibs.jqWidgets

import com.microsoft.playwright.Locator
import qed.testbaseclass.TestContext
import qed.uiwidgets.AtomicWidget
import qed.uiwidgets.widgetlibs.base.ITable

class jqWidgetsTable(context : TestContext, selector : String, val headerRowCount : Int = 1)  : ITable, AtomicWidget(context, selector)   {

    val id: String?
        get() = pageElement.getAttribute("id")

    override val headerLocators: List<Locator>
        get() = (0 until headerRowCount).map { pageElement.locator("div[id='columntable$id']").nth(it) }

    override fun getColumnIndex(headerText: String): Int? {
        val headerCells = headerLocators.flatMap {
            val loc = ">div"
            val count = it.locator(loc).count()
            (0 until count).map { col -> it.locator(loc).nth(col) }
        }
        headerCells.forEachIndexed { index, cell ->
            if (cell.innerText().trim().equals(headerText, ignoreCase = true)) {
                return index
            }
        }
        return null
    }

    override fun cell(rowIndex: Int, columnIndex: Int): Locator {
        val rowLocator = pageElement.locator("div[id='contenttable$id']>div[id='row$rowIndex$id']")
        val col = columnIndex + 1
        return rowLocator.locator("div:nth-of-type($col)>div")
    }

    override fun cellSetValue(rowIndex: Int, columnIndex: Int, value : String) {
        val rowLocator = pageElement.locator("div[id='contenttable$id']>div[id='row$rowIndex$id']")
        val col = columnIndex + 1
        rowLocator.locator("div:nth-of-type($col)>div input").fill(value)
    }


    override fun columnValues(columnIndex: Int): List<String> {
        // todo: filter elements where first div class doesn't contain jqx-grid-cleared-cell
        val returnVal = mutableListOf<String>()
        var row = 0
        pageElement.locator("div[id='contenttable$id']>div").all().forEach{
            if (!it.locator(">div:nth-of-type(1)").getAttribute("class").contains("jqx-grid-cleared-cell")) {
                returnVal.add(cell(row, columnIndex).textContent())
            }
            row++
        }
        return returnVal
    }

    override fun row(rowIndex: Int) = pageElement.locator("div[id='contenttable$id']>div").nth(rowIndex)

    override fun rowValues(rowIndex: Int) : List<String> {
        val returnVal = mutableListOf<String>()
        val row = row(rowIndex)
        val colCount = row.locator("div[role='gridcell']").all().count()
        (1 until colCount+1).map{returnVal.add(row.locator("div[role='gridcell']:nth-of-type($it)>div").innerText())}
        return returnVal
    }

    override fun rowsWith(constraints: List<Pair<Int, String>>) : List<Int> {
        val returnVal = mutableListOf<Int>()
        var row = 0
        pageElement.locator("div[id='contenttable$id']>div").all().forEach{line ->
            val hidden = line.locator(">div:nth-of-type(1)").getAttribute("class").contains("jqx-grid-cleared-cell")
            // initialise as true if not hidden, alse false
            var meetsAllConditions = !hidden
            if (!hidden) {
                constraints.forEach {pair ->
                    if (line.locator(">div:nth-of-type(${pair.first+1})").innerText() != pair.second) {
                        meetsAllConditions = false
                    }
                }
            }
            if (meetsAllConditions)
                returnVal.add(row)
            row++
        }
        return returnVal
    }

    override fun allRows(): List<Locator> {
        val returnVal =  mutableListOf<Locator>()
        var row = 0
        pageElement.locator("div[id='contenttable$id']>div").all().forEach{
            if (!it.locator(">div:nth-of-type(1)").getAttribute("class").contains("jqx-grid-cleared-cell")) {
                returnVal.add(it)
            }
            row++
        }
        return returnVal
    }

    // to support the following syntax in validations: expect(table[1, 2].textContent()).to.equal("Price")
    override operator fun get(rowIndex: Int, columnIndex: Int): Locator {
        return cell(rowIndex, columnIndex)
    }

    override fun locatorInRow(rowIndex: Int, selector: String): Locator =
        row(rowIndex).locator(selector)

}