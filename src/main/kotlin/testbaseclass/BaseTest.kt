package qed.testbaseclass

import ExtentConfig
import com.aventstack.extentreports.ExtentTest
import com.aventstack.extentreports.Status
import org.testng.SkipException
import org.testng.annotations.AfterMethod
import qed.reports.Logger
import qed.reports.QEDStatus
import java.lang.reflect.Method
import java.util.*

fun uniqueTestName() = UUID.randomUUID().toString() + System.currentTimeMillis().toString()

open class BaseTest(val uniqueClassName : String = uniqueTestName() ) : IBaseTest {

    override val logger = Logger()
    lateinit var environment : Map<String, String>
    lateinit var extentTest : ExtentTest
    lateinit var methodName : String
    lateinit var extent : ExtentConfig

    // configuration variables:
    val url: (String) -> String = { key ->
        try {
            environment.get(key).toString()
        } catch(e: Exception) {
            e.message.toString()
        }
    }

    @AfterMethod
    fun afterMethod(m : Method) {
        // this is for debugging purposes only. When stable, this can disappear.
        logger.info { "uniqueTestName=$uniqueClassName" }
        logger.info { "methodName=${m.name}" }
    }

    // for verifications, use WinterBE.Expekt: https://github.com/winterbe/expekt
    // when the verification passes, then log that it did so
    // otherwise, show the message first, and then re-trow the same error, so it is handled as intended
    override fun verify(objective: String, verificationBlock: () -> Unit) {
        try {
            verificationBlock.invoke()
            logger.verify(QEDStatus.PASS) { objective }
        } catch (e:AssertionError) {
            logger.verify(QEDStatus.FAIL) { objective }
            throw (e)
        } catch (e:SkipException) {
            logger.verify(QEDStatus.SKIP) { objective }
            throw (e)
        }
    }
}