![QED Logo](/images/QED_Logo.svg)
# QED – Quod Erat Demonstrandum

**QED** is a modern test automation framework built for engineers who value clarity, speed, and trust. Written in Kotlin and powered by expressive DSLs, QED turns test automation into something more than scripts—it becomes a declarative, maintainable, and evidence-driven practice.

The name comes from “Quod Erat Demonstrandum”—Latin for “that which was to be proven.” Traditionally, it closes a mathematical proof: a statement that starts as a question, travels through reasoning, and ends as truth. In the same way, QED closes the loop on software quality, turning intent into evidence so you can release with confidence.

And just as in proofs, every outcome has its mark:

✅ Passed tests: Quod erat demonstrandum — it has been proven.

⚠️ Skipped tests: Quaestio manet — the question remains.

❌ Failed tests: Investigandum est — further investigation is required.

With QED, every test tells a story, and every story ends with proof.

---

## 🔍 What is QED?

Apart from the Latin origin, QED stands for:

- **Quality Evidently Delivered** – automation that speaks for itself.
- **Quick Evaluation before Deployment** – fast, reliable feedback loops.
- **Quick Evidence Delivery** – fast test reporting results
- **Quality Evaluation DSL** - scripts can be written using a tailored DSL (Domain Specific Language)
- **Quality Engineering Directive** - standardised test automation methodology
- **Quality Evidence Document** – structured, readable test artifacts.
- **Quality Execution Driver** – your engine for robust, scalable testing.

Whether you're validating APIs, orchestrating UI flows, or integrating with CI/CD pipelines, QED helps you automate with confidence and precision.

---

## ✨ Key Features

- ✅ **Expressive Kotlin DSLs** for UI and API testing  
- 🧱 **Modular architecture** with clean separation of concerns  
- 🚀 **Fast execution** via Playwright, REST-assured, and TestNG  
- 📊 **Advanced reporting** with Allure and ExtentReports  
- 🔄 **CI/CD ready** with seamless Gradle integration  
- 🧠 **Designed for maintainability** long-term scalability  
- 🧱 **Based on SOLID principles** flexible to change, easy to extend, and simple to maintain  
---

## 🧪 Sample DSL (mixed UI and REST)

```kotlin
private val baseTest = BaseTest()
private val hasRest = HasRest(baseTest, urlKey = "apichallenges")
private val hasBrowser = HasBrowser(baseTest, urlKey = "uitestingurl")


class UI_RESTTest : TestContext(baseTest, hasBrowser, hasRest) {

    inline fun <reified T> Moshi.jsonAdapter(): JsonAdapter<T> =
        this.adapter(T::class.java)

    data class Payroll(
        val title: String,
        val doneStatus: Boolean,
        val description: String
    )

    @Test(priority=0, description = "mixed UI/API test)", groups = ["All"])
    fun testUI_Rest() {
        val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
        val payroll = Payroll("create todo process payroll", true, "todo list needs to be added")
        val json = moshi.jsonAdapter<Payroll>().toJson(payroll)
        logger.info { "opening UI Testing Playground" }
        val landingPage = GenericPage(UITestingPage(this))
        hasBrowser?.apply {
            navigateToApp()
            startFromPage(landingPage) {
                val result = rest.send(RequestType.POST, APIChalURLPath.SIM_ENTITIES, json, 201)
                logger.info { "API test within ui test" }
                logger.info { result }
                verify("check response body") {
                    expect(result.get("name").asText()).to.equal("bob")
                    expect(result.get("id").asInt()).to.equal(11)
                }
            }
        }
    }
    
}
```

## 🔍 Sample Report
![Screenshot of a single test execution report](/images/QED_Report.png)



