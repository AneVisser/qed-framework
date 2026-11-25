package qed.uiwidgets.widgetlibs.react

import com.microsoft.playwright.Locator
import qed.testbaseclass.TestContext
import qed.uiwidgets.AtomicWidget
import qed.uiwidgets.widgetlibs.base.ITable

/**
 * React Data Grid (Comcast/react-data-grid, used through lukra-ui's LukraDataGrid in the Lukra
 * and Livestock frontends), 2026-11-25. Moved here from qed-sut-livestock-ui's LukraGrid, which
 * packaged the pattern of Lukra's CalculatePage supplement methods.
 *
 * The grid renders div[role=grid] with role="row" / role="columnheader" / role="gridcell", not an
 * HTML <table>, so the base Table widget (tr/td) doesn't apply. Like jqWidgetsTable (the other
 * div-based grid), it implements ITable: rows and columns by number.
 *
 * Prefer the key-based methods (row(cellTestId, text), cellText, clickInRow, editCell): the
 * frontends give every column its own data-testid, and a row is found by a key cell's test id
 * plus its exact text, with Playwright's :text-is(). That survives sorting, filtering and
 * column reordering, which row and column numbers don't. (Not a Java Pattern.quote regex in
 * setHasText: Playwright runs filter patterns as JavaScript regexes, which don't know \Q...\E.)
 *
 * Editing (the frontends' convention, claude/shared-grid-lukra-ui.md): a cell shows text until
 * it's opened (double-click); the editor then appears inside the cell with the SAME
 * data-testid. So: dblclick the cell, fill the editor, press Enter (editCell).
 *
 * Virtual scrolling is off by default in LukraDataGrid, so every row is in the DOM. With
 * enableVirtualization on, rows outside the view aren't, and these methods won't find them.
 *
 * [selector]: the grid, e.g. "[data-testid='admin-invitations-grid']" (the testId given to
 * LukraDataGrid).
 */
class DataGrid(
    context: TestContext,
    selector: String,
    val stabilityWindowMs: Long = 300,
    val stabilityTimeoutMs: Long = 10_000,
) : ITable, AtomicWidget(context, selector) {

    // Raw locators, without AtomicWidget.pageElement's 5 second wait for the selector: an empty
    // list often shows a message instead of the grid, and "not there" is then a valid answer
    private fun page() = context.browser.page
    private val grid: Locator
        get() = page().locator(selector).first()

    /** Waits until the grid is in the page */
    fun waitUntilShown() = grid.waitFor()

    /** Whether the grid is in the page now (no waiting) */
    val isShown: Boolean
        get() = page().locator(selector).count() > 0

    // ── Rows ─────────────────────────────────────────────────────────────────

    private val dataRows: Locator
        get() = grid.locator("[role='row']:not(:has([role='columnheader']))")

    /**
     * Blocks until the number of data rows has been unchanged for [stabilityWindowMs], as
     * Table.waitForStableRows: after a filter change, delete or refresh, React re-renders the grid
     */
    internal fun waitForStableRows() {
        val deadline = System.currentTimeMillis() + stabilityTimeoutMs
        fun currentCount() = if (isShown) dataRows.count() else 0
        var stableCount = currentCount()
        var stableSince = System.currentTimeMillis()
        while (true) {
            val now = System.currentTimeMillis()
            if (now >= deadline) error("DataGrid '$selector' did not reach a stable row count within ${stabilityTimeoutMs}ms")
            if (now - stableSince >= stabilityWindowMs) return
            Thread.sleep(50)
            val count = currentCount()
            if (count != stableCount) {
                stableCount = count
                stableSince = System.currentTimeMillis()
            }
        }
    }

    // ── ITable: by row and column number ─────────────────────────────────────

    override val headerLocators: List<Locator>
        get() = listOf(grid.locator("[role='row']:has([role='columnheader'])").first())

    override fun getColumnIndex(headerText: String): Int? {
        val headers = grid.locator("[role='columnheader']")
        (0 until headers.count()).forEach { index ->
            if (headers.nth(index).innerText().trim().equals(headerText, ignoreCase = true)) return index
        }
        return null
    }

    override fun row(rowIndex: Int): Locator = dataRows.nth(rowIndex)

    override fun cell(rowIndex: Int, columnIndex: Int): Locator =
        row(rowIndex).locator("[role='gridcell']").nth(columnIndex)

    /** Opens the cell's editor (double-click), fills it and commits with Enter */
    override fun cellSetValue(rowIndex: Int, columnIndex: Int, value: String) =
        fillEditor(cell(rowIndex, columnIndex), "input, textarea", value)

    override fun columnValues(columnIndex: Int): List<String> {
        waitForStableRows()
        return (0 until dataRows.count()).map { cell(it, columnIndex).innerText().trim() }
    }

    override fun rowValues(rowIndex: Int): List<String> {
        val cells = row(rowIndex).locator("[role='gridcell']")
        return (0 until cells.count()).map { cells.nth(it).innerText().trim() }
    }

    override fun rowsWith(constraints: List<Pair<Int, String>>): List<Int> {
        waitForStableRows()
        return (0 until dataRows.count()).filter { rowIndex ->
            constraints.all { (column, text) -> cell(rowIndex, column).innerText().trim() == text }
        }
    }

    override fun allRows(): List<Locator> {
        waitForStableRows()
        return (0 until dataRows.count()).map { dataRows.nth(it) }
    }

    // Supports: expect(grid[1, 2].textContent()).to.equal("Price")
    override operator fun get(rowIndex: Int, columnIndex: Int): Locator = cell(rowIndex, columnIndex)

    override fun locatorInRow(rowIndex: Int, selector: String): Locator = row(rowIndex).locator(selector)

    // ── By content: a key cell's test id plus its exact text ─────────────────

    /** Selector of the cell with test id [cellTestId] whose text is exactly [text] */
    fun cellSelector(cellTestId: String, text: String): String {
        val escaped = text.replace("\\", "\\\\").replace("\"", "\\\"")
        return "[data-testid='$cellTestId']:text-is(\"$escaped\")"
    }

    /**
     * The row whose [keyCellTestId] cell shows exactly [keyText].
     * The `has` locator is matched INSIDE each candidate row, so it must be a page-level locator,
     * never one that starts from the grid: that would look for the whole grid inside the row and
     * never match (the bug of 30 Oct 2026 in Lukra's CalculatePage).
     */
    fun row(keyCellTestId: String, keyText: String): Locator =
        grid.locator("[role='row']")
            .filter(Locator.FilterOptions().setHas(page().locator(cellSelector(keyCellTestId, keyText))))
            .first()

    /** Whether a row with that key cell text is shown now (no waiting) */
    fun hasRow(keyCellTestId: String, keyText: String): Boolean =
        isShown && grid.locator(cellSelector(keyCellTestId, keyText)).count() > 0

    /** Number of data rows, counted by a column every row has ([keyCellTestId]) */
    fun rowCount(keyCellTestId: String): Int =
        if (isShown) grid.locator("[data-testid='$keyCellTestId']").count() else 0

    /** The text of the [cellTestId] cell in the row found by its key cell */
    fun cellText(keyCellTestId: String, keyText: String, cellTestId: String): String =
        row(keyCellTestId, keyText).locator("[data-testid='$cellTestId']").first().innerText().trim()

    /**
     * Like [cellText], but waits (up to [timeoutMs]) until the text is [expected], for cells that
     * change after an action (e.g. a status after a revoke). Returns the last text read, so the
     * test's verification shows what was there instead
     */
    fun waitForCellText(
        keyCellTestId: String, keyText: String, cellTestId: String, expected: String,
        timeoutMs: Long = 10_000
    ): String {
        val deadline = System.currentTimeMillis() + timeoutMs
        var text = cellText(keyCellTestId, keyText, cellTestId)
        while (text != expected && System.currentTimeMillis() < deadline) {
            Thread.sleep(100)
            text = cellText(keyCellTestId, keyText, cellTestId)
        }
        return text
    }

    /** Clicks the element [selector] inside the row found by its key cell (e.g. a row's button) */
    fun clickInRow(keyCellTestId: String, keyText: String, selector: String) =
        row(keyCellTestId, keyText).locator(selector).first().click()

    /**
     * Edits the [cellTestId] cell of the row found by its key cell: double-click opens the editor,
     * which has the same data-testid; fill it and commit with Enter (as CalculatePage.ingredientPrice)
     */
    fun editCell(keyCellTestId: String, keyText: String, cellTestId: String, value: String) {
        val row = row(keyCellTestId, keyText)
        row.locator("[data-testid='$cellTestId']").first().dblclick()
        val editor = row.locator("input[data-testid='$cellTestId'], textarea[data-testid='$cellTestId']").first()
        editor.fill(value)
        editor.press("Enter")
    }

    /** Double-click [cell], then fill the first [editorSelector] inside it and press Enter */
    private fun fillEditor(cell: Locator, editorSelector: String, value: String) {
        cell.dblclick()
        val editor = cell.locator(editorSelector).first()
        editor.fill(value)
        editor.press("Enter")
    }
}
