# Page Objects

QED's DSL supports expressive, modular UI testing through idiomatic Page Objects. These objects encapsulate screen structure, expose interactive elements, and integrate seamlessly with the DSL — enabling readable, maintainable tests without ceremony.

---

## DSL Integration

When used inside `onPage(<PageObject>) { ... }`, all **public properties and functions** of the page object become available within the DSL block.

```kotlin
onPage(loginPage) {
    username = "alice"
    password = "secret"
    loginButton.click()
}
```

- `username`, `password`, and `loginButton` are public properties of `loginPage`
- The DSL automatically scopes interactions to the current page
- No need for explicit selectors or driver calls

---

## Defining a Page Object

Page objects are regular Kotlin classes with public properties representing UI elements:

```kotlin
class LoginPage : Page {
    var userName by InputFieldDelegate(context, "input[id='username']")
    var password by InputFieldDelegate(context, "input[id='password']")
    val loginButton = button("login")
}
```

Using a delegate allows direct value assignment in the DSL — `username = "alice"` rather than `username.value = "alice"`. Without a delegate, the property holds the element itself:

```kotlin
class LoginPage : Page {
    var userName = InputField(context, "input[id='username']")
}
```

---

## Factory Elements & JS Library Support

QED supports factory-based element resolution, where the factory determines which JS library is in use and returns an element with matching behaviour.

To use factory-based resolution, use the lowercase form of the element name (`inputField` instead of `InputField`):

```kotlin
class LoginPage : Page {
    var userName = inputField(context, "input[id='username']")
    var password by inputFieldDelegate(context, "input[id='password']")
}
```

The JS library used by the application is declared in the configuration file:

```json
"widgettype": "JQWIDGETS"
```

**Supported libraries**

- React
- JQWidgets
- Others can be added via factory extension

---

## Screen Area Composition

Page objects can be composed of page areas or sub-pages. Shared screen regions — such as a top bar, sidebar, or info panel — only need to be defined once and can be reused across multiple page compositions.

```kotlin
val topbar = TopBar(context)
val sidebarDesktop = SideBarDesktop(context)
val infoBar = InfoBar(context)
val summaryPage = GenericPage(SummaryPage(context, topbar, sidebarDesktop, infoBar))

onPage(summaryPage) {
    header.userMenu.click()
    todoList.selectItem("Payroll")
    createButton.click()
}
```

- Encourages modular design
- Shared regions are defined once and composed where needed
- Keeps DSL usage clean and intention-revealing

---

## Best Practices

Following these conventions keeps page objects maintainable and the DSL readable:

- Keep page objects small and focused on a single screen or area
- Compose shared regions into reusable components rather than duplicating element definitions
- Use factory elements for dynamic or JS library-specific widgets
- Use descriptive property names that reflect user intent, not implementation detail
- Treat page objects as a public API — don't expose internal selectors or driver mechanics

---

## Roadmap

Factory element support is under active development. Planned improvements include:

- Custom element behaviours per JS library
- Enhanced introspection and diagnostics
- Unified interaction API across supported libraries
- Plug-in architecture for adding JS library adapters