package qed.testbaseclass

import ExtentConfig
import QedConfig
import WidgetType
import com.aventstack.extentreports.reporter.configuration.Theme
import org.testng.annotations.*
import qed.helper.MailpitHelper
import qed.json.QEDJson
import qed.reports.ExtentManager
import qed.reports.ExtentTestListener
import qed.reports.PerformanceReportListener
import java.io.File
import java.lang.reflect.Method

/**
 * TestContext is the class that contains the properties for UI and API tests, which can be swithed on or off when needed.
 * It should always include an instance of BaseTest, as that has all the properties related to threads and reporting of thread
 * activities (see listener for reporting of test threads in ExtentReorts).
 * For mixed API and UI tests, both hasBrowser and hasRest should be instantiated, and then the properties of these classes
 * become available in the tests.
 */
@Listeners(ExtentTestListener::class, PerformanceReportListener::class)

open class TestContext(
    val baseTest: BaseTest,
    var hasBrowser: HasBrowser? = null,
    val hasRest: HasRest? = null,
    val pageRegistry: PageRegistry? = null
) : IBaseTest by baseTest, IHasBrowser, IHasRest {

    init {
        pageRegistry?.context = this
    }

    // Instantiated once in @BeforeSuite after config is loaded
    var mailpitHelper: MailpitHelper? = null

    override val browser: BrowserDriver
        get() = hasBrowser?.browser
            ?: throw IllegalStateException("Browser is not available in this context")

    override val rest: RestClient
        get() = hasRest?.rest
            ?: throw IllegalStateException("REST client not available in this context")

    fun changeUrl(urlKey: String) {
        changeRestUrl(urlKey)
        changeBrowserUrl(urlKey)
    }

    fun changeBrowserUrl(urlKey: String) {
        hasBrowser?.urlKey = urlKey
    }

    fun changeRestUrl(urlKey: String) {
        hasRest?.urlKey = urlKey
        hasRest?.rest?.url = baseTest.url(urlKey)
    }

    private fun loadConfig(configfile: String) {
        val env = System.getProperty("env.name", "dev")
        val config = QEDJson.fromJson<QedConfig>(File(configfile).readText())
        val environment = config?.environments?.get(env) ?: throw IllegalStateException(
            "Environment '$env' not found in config file '$configfile'. " +
                    "Valid environments: ${config?.environments?.keys?.joinToString()}"
        )
        baseTest.envURLs = environment
        baseTest.configEnv = env
        hasBrowser?.widgetType = WidgetType.fromString(config.widgettype.toString())
        hasBrowser?.browserName = BrowserName.entries.first { it.brwsr == config.browser.toString() }
        baseTest.extent = config.reporting?.extent ?: ExtentConfig("STANDARD", null, "", true)
        ExtentManager.rptTheme = Theme.entries.first { it.name == baseTest.extent.theme?.uppercase() }
        ExtentManager.fontcolour = when (ExtentManager.rptTheme) {
            Theme.DARK -> "#b3be62"
            Theme.STANDARD -> "#214478"
        }
        ExtentManager.reportname = baseTest.extent.reportName ?: "Test Execution Report"
        ExtentManager.documenttitle = baseTest.extent.documentTitle ?: "Test Automation Report"
        ExtentManager.enableScreenshots = baseTest.extent.enableScreenshots ?: true

        // Instantiate MailpitHelper once config is available
        mailpitHelper = config.mailpitUrl?.let { MailpitHelper(it) }
    }

    @BeforeSuite
    fun beforeSuite() {
        // intentionally empty — kept for future suite-level setup
        // config is loaded in beforeMethod to guarantee it precedes any test thread work
    }

    @Volatile
    private var configLoaded = false
    private val configLock = Any()


    /**
     * Loads the test configuration exactly once before the first test method runs,
     * regardless of how many test classes or threads are active. Uses double-checked
     * locking so that parallel threads block until config is ready rather than each
     * attempting their own load.
     *
     * Config is read from the XML suite parameter 'configfile', which points to the
     * environment-specific JSON config for the SUT. The environment is selected via
     * the Gradle property -Penvironment (default: dev).
     *
     * After config is loaded on the first call, subsequent calls only set the test
     * method name for reporting purposes.
     */
    @Parameters("configfile")
    @BeforeMethod
    fun beforeMethod(m: Method, configfile: String) {
        if (!configLoaded) {
            synchronized(configLock) {
                if (!configLoaded) {  // double-checked locking
                    loadConfig(configfile)
                    mailpitHelper?.clearInbox()
                    configLoaded = true
                }
            }
        }
        baseTest.methodName = m.name
    }

    /**
     * Closes and removes the browser driver for the current thread after each test method.
     * Called even if the test failed (alwaysRun = true).
     * Each test method gets a fresh browser instance on the next run via the ThreadLocal
     * in HasBrowser — this ensures no state leaks between test methods.
     */
    @AfterMethod(alwaysRun = true)
    fun closeBrowser() {
        hasBrowser?.browser?.finalise()
        hasBrowser?.removeBrowserDriver()
    }

    /**
     * Diagnostic: prints all non-daemon threads still alive after the suite completes.
     * Useful for diagnosing suite hangs caused by threads that did not shut down cleanly.
     */
    @AfterSuite(alwaysRun = true)
    fun reportThreads() {
        val alive = Thread.getAllStackTraces().keys
            .filter { !it.isDaemon && it != Thread.currentThread() }
        println("=== Non-daemon threads still alive ===")
        alive.forEach { t ->
            println("${t.name} - ${t.state}")
            t.stackTrace.forEach { ste ->
                println("    at $ste")
            }
        }
    }
}


