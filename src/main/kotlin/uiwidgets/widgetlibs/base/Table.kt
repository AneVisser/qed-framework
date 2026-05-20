package qed.uiwidgets.widgetlibs.base

import com.microsoft.playwright.Locator
import qed.testbaseclass.TestContext
import qed.uiwidgets.AtomicWidget

class Table(
    context: TestContext,
    selector: String,
    val headerRowCount: Int = 1,
    val stabilityWindowMs: Long = 300,
    val stabilityTimeoutMs: Long = 10_000,
) : ITable, AtomicWidget(context, selector) {

    // ── Stability polling ─────────────────────────────────────────────────────

    /**
     * Blocks until the table's row count has been unchanged for [stabilityWindowMs].
     * Useful after async operations (pagination, delete, tab switch) that cause
     * React (or any SPA framework) to re-render the table contents.
     * Can be called explicitly from a page object when a specific wait is needed,
     * but prefer relying on the read methods below, which call it automatically.
     */
    internal fun waitForStableRows() {
        val pollIntervalMs = 50L
        val deadline = System.currentTimeMillis() + stabilityTimeoutMs
        var stableCount = pageElement.locator("tr").count()
        var stableSince = System.currentTimeMillis()

        while (true) {
            val now = System.currentTimeMillis()
            if (now >= deadline) error("Table '$selector' did not reach a stable row count within ${stabilityTimeoutMs}ms")
            if (now - stableSince >= stabilityWindowMs) return  // stable long enough

            Thread.sleep(pollIntervalMs)

            val currentCount = pageElement.locator("tr").count()
            if (currentCount != stableCount) {
                // Row count changed — reset the stability clock
                stableCount = currentCount
                stableSince = System.currentTimeMillis()
            }
        }
    }

    // ── Header helpers ────────────────────────────────────────────────────────

    override val headerLocators: List<Locator>
        get() = (0 until headerRowCount).map { pageElement.locator("tr").nth(it) }

    override fun getColumnIndex(headerText: String): Int? {
        val headerCells = headerLocators.flatMap {
            val count = it.locator("th, td").count()
            (0 until count).map { col -> it.locator("th, td").nth(col) }
        }
        headerCells.forEachIndexed { index, cell ->
            if (cell.innerText().trim().equals(headerText, ignoreCase = true)) {
                return index
            }
        }
        return null
    }

    // ── Cell / row access ─────────────────────────────────────────────────────

    override fun cell(rowIndex: Int, columnIndex: Int): Locator {
        val rowLocator = pageElement.locator("tr").nth(rowIndex + headerRowCount)
        return rowLocator.locator("td").nth(columnIndex)
    }

    override fun cellSetValue(rowIndex: Int, columnIndex: Int, value: String) {
        val rowLocator = pageElement.locator("tr").nth(rowIndex + headerRowCount)
        rowLocator.locator("td").nth(columnIndex).locator(" input").fill(value)
    }

    override fun row(rowIndex: Int) = pageElement.locator("tr").nth(rowIndex + headerRowCount)

    override fun rowValues(rowIndex: Int): List<String> {
        val returnVal = mutableListOf<String>()
        val row = row(rowIndex)
        val colCount = row.locator("td").all().count()
        (0 until colCount).map {
            returnVal.add(row.locator("td").nth(it).innerText())
        }
        return returnVal
    }

    // ── Read methods (stability-guarded) ──────────────────────────────────────

    override fun columnValues(columnIndex: Int): List<String> {
        waitForStableRows()
        val rowCount = pageElement.locator("tr").count()
        return (headerRowCount until rowCount).map { rowIdx ->
            val row = pageElement.locator("tr").nth(rowIdx)
            row.locator("td").nth(columnIndex).innerText().trim()
        }
    }

    override fun rowsWith(constraints: List<Pair<Int, String>>): List<Int> {
        waitForStableRows()
        val returnVal = mutableListOf<Int>()
        val rowCount = pageElement.locator("tr").count()
        (0 until rowCount - headerRowCount).map {
            var meetsAllConditions = true
            constraints.forEach { pair ->
                if (cell(it, pair.first).innerText() != pair.second) {
                    meetsAllConditions = false
                }
            }
            if (meetsAllConditions) {
                returnVal.add(it)
            }
        }
        return returnVal
    }

    override fun allRows(): List<Locator> {
        waitForStableRows()
        val rowCount = pageElement.locator("tr").count()
        return (0 until rowCount - headerRowCount).map { pageElement.locator("tr").nth(it) }
    }

    // ── Operator shorthand ────────────────────────────────────────────────────

    // Supports: expect(table[1, 2].textContent()).to.equal("Price")
    override operator fun get(rowIndex: Int, columnIndex: Int): Locator {
        return cell(rowIndex, columnIndex)
    }
}