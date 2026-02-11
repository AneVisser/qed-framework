# Page Objects

QED’s DSL supports expressive, modular UI testing through idiomatic Page Objects. These objects encapsulate screen structure, expose interactive elements, and integrate seamlessly with the DSL — enabling readable, maintainable tests without ceremony.

---

## DSL Integration

When used inside `onPage(<PageObject>) { ... }`, all **public properties and functions** of the page object become available within the DSL block.

### Example

```kotlin
onPage(loginPage) {
    username = "alice"
    password = "secret"
    loginButton.click()
}
```
- usernameInput, passwordInput, and loginButton are public properties of loginPage
- The DSL automatically scopes interactions to the current page
- No need for explicit selectors or driver calls

## Defining a Page Object
Page objects are regular Kotlin classes with public properties representing UI elements:
```kotlin
class LoginPage : Page {
    var userName by InputFieldDelegate(context, "input[id='username']")
    var password by InputFieldDelegate(context,"input[id='password']")
    val loginButton = button("login")
}
```
In the above example, username and password are defined as delegates to
TextInput. Because of that, we can assign a value directly to username,
rather than to username.value, if it was defined as follows:
```kotlin
class LoginPage : Page {
    var userName = InputField(context, "input[id='username']")
}
```
## Factory Elements & JS Library Support
QED supports factory-based element resolution, where the factory determines which JS library is in use and returns an element with matching behavior.

To use factory-based element resolution, use the class names in lower case (so <u>**i**</u>nputField instead of <u>**I**</u>nputField:
```kotlin
class LoginPage : Page {
    var userName = inputField(context, "input[id='username']")
    var password by inputFieldDelegate(context,"input[id='password']")
}
```
Supported Libraries (Early Stage)
- React
- JQWidgets
- Others can be added via factory extension
The library used for an application is defined in the configuration file:
```json
  "widgettype" : "JQWIDGETS"
```
  
## Future Direction
- Plug-in architecture for JS library adapters

## Limitations & Roadmap  
- Factory element support is under development
- React and JQWidgets are supported; others require adapter implementation
- Future versions will support:
  - Custom element behaviors per library
  - Enhanced introspection and diagnostics
  - Unified interaction API across libraries