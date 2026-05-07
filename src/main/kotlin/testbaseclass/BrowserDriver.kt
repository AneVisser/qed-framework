package qed.testbaseclass

import com.aventstack.extentreports.MediaEntityBuilder
import com.aventstack.extentreports.model.Media
import com.microsoft.playwright.*
import java.nio.file.Paths
import java.text.SimpleDateFormat
import java.util.*

/**
 * This class drives everything related to playwright, i.e. instantiation and finalisation,
 * and also exposes the variable page, which is used to navigate.
 */



class BrowserDriver(val uniqueClassName : String, val browserName: BrowserName) {

    var playwright: Playwright = Playwright.create()
    // Run headless on CI or when env var QED_HEADLESS is set; headed otherwise
    val headless = System.getenv("QED_HEADLESS")?.equals("true", ignoreCase = true) ?: false

    var browser: Browser = browserType
        .launch(
            BrowserType.LaunchOptions()
                .setHeadless(headless)
                .setArgs(mutableListOf(
                    "--start-maximized",
                    "--no-sandbox",
                    "--disable-setuid-sandbox"
                ))
        )

    var context: BrowserContext = browser.newContext()

    var page: Page = context.newPage()

    init {
        page.setViewportSize(1920, 1200)
    }

    protected val browserType: BrowserType get() =
        when (browserName) {
            BrowserName.CHROMIUM -> playwright.chromium()
            BrowserName.WEBKIT -> playwright.webkit()
            BrowserName.FIREFOX -> playwright.firefox()
        }

    // snapshots can only be made when there is a browser open; by default to Extent Reports directory
    data class ScreenshotResult(
        val media: Media,         // For ExtentReports
        val bytes: ByteArray      // For raw use (potentially other reporting tools)
    )
    fun addScreenShot(subdir : String = "\\test-output\\ExtentReport\\images"): ScreenshotResult {
        val testMethod = uniqueClassName.lastWord('.')
        val tst = testMethod?.replace(" ", "_")
        val dateFormat = SimpleDateFormat("dd-MMM-yyyy_HH-mm-ss")
        val date = Date()
        val timeStamp = dateFormat.format(date).toString()
        val physicalPath =
            System.getProperty("user.dir") + "\\build$subdir\\${tst}_$timeStamp.png"
        val htmlPath = "images/${tst}_$timeStamp.png"
        // create physical file
        val bytes = page.screenshot(Page.ScreenshotOptions().setPath(Paths.get(physicalPath)))
        // embed screenshot in html
        val media = MediaEntityBuilder.createScreenCaptureFromPath(htmlPath).build()
        return ScreenshotResult(media, bytes)
    }

    fun finalise() {
        page.close()
        context.close()
        browser.close()
        playwright.close()

    }

}


object ScreenshotProvider {
    fun capture(page: Page): ByteArray? {
        return try {
            page.screenshot().toString().toByteArray()
        } catch (e: Exception) {
            println("[QED] Screenshot capture failed: ${e.message}")
            null
        }
    }
}
