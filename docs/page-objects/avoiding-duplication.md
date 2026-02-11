
Page objects can be composed of page area's or sub-pages. This way, for different
pages with shared elements, the page elements within page area's only need to be
defined once, so they can be re-used in other compositions.

**Example: Composed page object**
```kotlin
    val topbar = TopBar(context)
    val sidebardesktop = SideBarDesktop(context)
    val infoBar = InfoBar(context)
    val summaryPage = GenericPage(SummaryPage(context, topbar, sidebardesktop, infoBar))
    onPage(summaryPage) {
        header.userMenu.click()
        todoList.selectItem("Payroll")
        createButton.click()
    }
```

- Encourages modular design
- Keeps DSL usage clean and intention-revealing