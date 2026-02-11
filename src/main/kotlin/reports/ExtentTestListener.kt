package qed.reports

import com.aventstack.extentreports.Status
import com.aventstack.extentreports.markuputils.ExtentColor
import com.aventstack.extentreports.markuputils.MarkupHelper
import org.testng.ITestContext
import org.testng.ITestListener
import org.testng.ITestResult
import qed.json.QEDJson
import qed.testbaseclass.TestContext
import qed.testbaseclass.lastWord
import java.io.IOException

/**
 * TestNG listener, which implements the actions to be executed for all test events. If the testContext contains a browser,
 * it has the ability to trigger adding a screenshot to a report page.
 */

class ExtentTestListener : ITestListener {

    override fun onTestStart(result: ITestResult) {
        val testName = result.method.methodName
        val className = result.testClass.name.lastWord('.')

        val testMethod = result.instanceName.lastWord('.')
        val annotation = result.method.qualifiedName.lastWord('.')
        val currentTest = result.instance as TestContext

        val thisTestName = currentTest.baseTest.uniqueClassName
        if (annotation=="beforeMethod") {
            println("testMethod: $testMethod")
            println("annotation: @$annotation")
            println("uniqueTestName: $thisTestName")
        }
        var description = result.method.description
        if (description.isNullOrBlank()) {
            description = result.name
        }

        // Create a report entry
        val test = ExtentManager.createTest("$description ($className::$testName)")
        test.log(Status.INFO, "Test started: $description")
        currentTest.baseTest.extentTest = test
    }

    override fun onTestSuccess(result: ITestResult) {
        val test = ExtentManager.getTest()
        val instance = result.instance
        if ((instance as TestContext).hasBrowser != null && ExtentManager.enableScreenshots) {
            val media = instance.browser.addScreenShot().media
            test?.pass(media)
        }
        if (result.status == ITestResult.SUCCESS) {
            ExtentManager.getTest()?.log(
                Status.PASS,
                "Test case <b>${result.name}</b> passed. <b class=\"montserrat\" style=\"font-size: 18px; color: ${ExtentManager.fontcolour};\">Quod erat demonstrandum</b>")
        }
    }

    override fun onTestFailure(result: ITestResult) {
        val test = ExtentManager.getTest()
        val instance = result.instance
        if (result.status == ITestResult.FAILURE) {
            test?.log(
                Status.FAIL,
                MarkupHelper.createLabel(result.throwable.toString() + " - Test Case Failed", ExtentColor.RED)
            )
            val throwable = result.throwable
            if (throwable != null) {
                // This will log the throwable and stacktrace in ExtentReports
                test?.fail(throwable)
            } else {
                test?.fail("Test failed without exception details.")
            }
        }
        // Screenshot if browser is used
        if ((instance as TestContext).hasBrowser != null && ExtentManager.enableScreenshots) {
            try {
                val media = instance.browser.addScreenShot().media
                test?.fail(media)
            } catch (ex: Exception) {
                test?.log(Status.WARNING, "Failed to capture screenshot: ${ex.message}")
            }
        }
        test?.log(
            Status.FAIL,
            "Test case <b>${result.name}</b> failed. <b class=\"montserrat\" style=\"font-size: 18px; color: ${ExtentManager.fontcolour};\">Investigandum est</b>"
        )
    }

    override fun onTestSkipped(result: ITestResult) {
        if (result.status == ITestResult.SKIP) {
            val test = ExtentManager.getTest()
            test?.log(
                Status.SKIP,
                "Test case <b>${result.name}</b> skipped. <b class=\"montserrat\" style=\"font-size: 18px; color: ${ExtentManager.fontcolour};\">Quaestio manet</b>"
            )
            val throwable = result.throwable
            if (throwable != null) {
                // This will log the throwable and stacktrace in ExtentReports
                test?.skip(throwable)
            } else {
                test?.skip("Test skipped without exception details.")
            }

        }
    }

    override fun onFinish(context: ITestContext) {
        try {
            val testResult: MutableMap<String, Any> = HashMap()
            testResult["TotalTestCaseCount"] = context.allTestMethods.size
            testResult["PassedTestCaseCount"] = context.passedTests.size()
            testResult["FailedTestCaseCount"] = context.failedTests.size()
            testResult["SkippedTestCaseCount"] = context.skippedTests.size()

            val json = QEDJson.prettyJson(testResult)
            ExtentManager.htmlReporter.file.writeText(json)

        } catch (e: IOException) {
            throw RuntimeException(
                "Error occurred while writing to TestExecutionReport.json file: ",
                e
            )
        }
        ExtentManager.flush()
    }

    // Other hook not needed
    override fun onStart(context: ITestContext) {}

    override fun onTestFailedButWithinSuccessPercentage(result: ITestResult) {
        ExtentManager.getTest()?.log(Status.INFO, "Test case failed but within success percentage: " + result.name)
    }


}