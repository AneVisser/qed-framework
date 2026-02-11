package qed.reports

import com.aventstack.extentreports.ExtentReports
import com.aventstack.extentreports.ExtentTest
import com.aventstack.extentreports.reporter.ExtentSparkReporter
import com.aventstack.extentreports.reporter.configuration.Theme
import java.util.concurrent.ConcurrentHashMap

object ExtentManager {

    // css for performance tables
    val colour_header_border
        get() = when (rptTheme) {
            Theme.STANDARD -> "#333"
            Theme.DARK -> "#5b6371"
        }
    val colour_header_background
        get() = when(rptTheme) {
            Theme.STANDARD -> "#f2f2f2"
            Theme.DARK -> "#1a222d"
        }
    val colour_separator
        get() = when(rptTheme) {
            Theme.STANDARD -> "#ccc"
            Theme.DARK -> "#5b6371"
        }
    val css_performance
        get() =
         """
         table.perf-table thead tr th {
            text-transform: none !important;
            font-size: 1.05em;       /* Slightly larger than default */
            font-weight: 600;        /* Semi-bold, cleaner than full bold */
            border-bottom: 0px !important;
         }
        .perf-table {
            border: 2px solid $colour_header_border; 
            border-collapse: collapse;
            border-spacing: 0;
            width: 100%;
            margin-top: 1.2em;
        }
        .perf-summary {
            background-color: $colour_header_background !important;
            margin-bottom: 0px;
            border-bottom: 0px;
        }
        .perf-detail {
            margin-top: 0px;
            border-top: 0px !important;
        }
        .perf-detail tbody, .perf-detail tr  {
            border-top: 0px !important;
        }
        .perf-table th, .perf-table td {
            border:none;
            padding: 6px 10px;
            text-align: left;
        }
        .perf-table thead {
            border-top: 1px solid $colour_header_border; 
        }
        .perf-table tbody.section-header td {
            font-weight: bold;
            background-color: #e9e9e9;
            border-top: 2px solid #aaa;
        }
        .perf-bar {
            color: #4CAF50;
            font-family: monospace;
            letter-spacing: 1px;
        }
        .col-endpoint { width: 150px; }
        .col-reqtype { width: 150px; }
        .col-numobs { width: 80px; }
        .col-avg { width: 130px; }
        .col-fastest { width: 100px; }
        .col-slowest { width: 100px; }
        .col-std { width: 100px; }
        .col-vc { width: auto; }
        .col-method { width: 180px; }
        .col-duration { width: 110px; }
        .col-timestamp { width: 180px; }
        .col-commit { width: 130px; }
        .col-timeline { width: auto; }
        .bar-container {
            background-color: #eee;
            height: 16px;
            width: 100%;
            border-radius: 4px;
            overflow: hidden;
        }
        .bar-fill {
            height: 100%;
            background-color: $fontcolour;
            border-radius: 4px 0 0 4px;
            transition: width 0.3s ease;
        }		
        """.trimIndent()
    // css for text representation of performance tables
    val css_monospace =
        """
        .monospace-block p {
            font-family: Courier, monospace !important;
            white-space: pre-wrap;
            margin: 0;
        }
        .monospace-block {
            font-family: Courier, monospace !important;
            white-space: pre-wrap;
        }
        """.trimIndent()
    // standard Extent reports show 'info' at every logline, which is a bit too chatty
    val css_hide_info =
        """
        .info-bg { display: none; }
        """.trimIndent()
    val css_qed_branding =
        """
            .info-bg { background-color: #FFFFFF; color: #FFFFFF; }
            @font-face {
              font-family: 'Montserrat';
              src: url('fonts/montserrat-v31-latin-regular.woff2') format('woff2');
              font-weight: normal;
              font-style: normal;
            }
            .montserrat {
              font-family: 'Montserrat', sans-serif;
            }
        """.trimIndent()
    val css_performance_bars =
        """
            .perf-fast { background-color: #4CAF50; !important;}   /* green */
            .perf-medium { background-color: #CEBC49; !important; } /* amber */
            .perf-slow { background-color: #F44336; !important;}   /* red */
        """.trimIndent()
    lateinit var htmlReporter: ExtentSparkReporter
    lateinit var extent: ExtentReports
    lateinit var rptTheme : Theme
    lateinit var reportname : String
    lateinit var documenttitle : String;
    var enableScreenshots : Boolean = true
    lateinit var fontcolour : String

    private val ext_reports: ExtentReports by lazy { createInstance() }

    // Thread-safe storage of tests per test thread
    private val testMap = ConcurrentHashMap<Long, ExtentTest>()

    private fun setExtent() {
        htmlReporter = ExtentSparkReporter(
            System.getProperty("user.dir") + "/build/test-output/ExtentReport/"
                    + "TestExecutionReport"
//                    + timeStamp
                    + ".html"
        )
// Load extent-config.xml from framework's classpath resources
        val configStream = this::class.java.getResourceAsStream("/extent-config.xml")
            ?: throw IllegalStateException("extent-config.xml not found on classpath")
        val tempConfig = java.io.File.createTempFile("extent-config", ".xml").apply {
            deleteOnExit()
            outputStream().use { configStream.copyTo(it) }
        }
        htmlReporter.loadXMLConfig(tempConfig.absolutePath)
        // css for images to click through on
//        val css = "img { border: 1px solid #f6f7fa;  width: 200px; } .info-bg { background-color: #FFFFFF; color: #FFFFFF; }"
        val css =
            """
            img { border: 1px solid #f6f7fa; width: 200px; }
            $css_qed_branding
            $css_hide_info
            $css_monospace
            $css_performance
            $css_performance_bars
            """.trimIndent()
        htmlReporter.config().css = css
        htmlReporter.config().css = css
        htmlReporter.config().apply {
            documentTitle = documenttitle
            reportName = reportname
            theme = rptTheme
        }

        extent = ExtentReports()
        extent.attachReporter(htmlReporter)
    }


    private fun createInstance(): ExtentReports {
        setExtent()
        return ExtentReports().apply {
            attachReporter(htmlReporter)
            setSystemInfo("Framework", "qed")
            setSystemInfo("Environment", "QA")
        }
    }

    fun getReports(): ExtentReports = ext_reports

    fun createTest(testName: String, description: String? = null): ExtentTest {
        val test = if (description != null) {
            ext_reports.createTest(testName, description)
        } else {
            ext_reports.createTest(testName)
        }

        testMap[Thread.currentThread().threadId()] = test
        return test
    }

    fun getTest(): ExtentTest? {
        return testMap[Thread.currentThread().threadId()]
    }

    fun flush() {
        ext_reports.flush()
    }
}