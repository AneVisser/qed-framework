package qed.reports

import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging

/**
 * The logger needs the testBaseClass to which the extentReport is attached, so that the logging statements end up
 * in the correct test. It also logs the message to the standard output.
 */

val indentSize = 1

open class Logger(logger : KLogger = KotlinLogging.logger{ },
                  private val reportSinks: List<IReportSink> = listOf(ReportSink_Extent() /* add other report sinks here. */ ) // hard-coded for now; should depend on config
) : KLogger by logger {

    var currentIndent : Int = 0;

    val indentation : String
        get() = "&emsp;".repeat(indentSize * currentIndent)

    override fun info(message: () -> Any?) {
        super.info{ indentation + message }
        reportSinks.forEach { it.log(QEDStatus.INFO, message().toString()) }
    }

    override fun trace(message: () -> Any?) {
        super.trace{ indentation + message }
        reportSinks.forEach { it.log(QEDStatus.INFO, message().toString()) }
    }

    override fun debug(message: () -> Any?) {
        super.debug{ indentation + message }
        reportSinks.forEach { it.log(QEDStatus.INFO, message().toString()) }
    }

    override fun warn(message: () -> Any?) {
        super.warn{ indentation + message }
        reportSinks.forEach { it.log(QEDStatus.WARN, message().toString()) }
    }

    override fun error(message: () -> Any?) {
        super.error{ indentation + message }
        reportSinks.forEach { it.log(QEDStatus.FAIL, message().toString()) }
    }

    fun indent() {
        currentIndent += 1
    }

    fun outdent() {
        currentIndent -= 1
        if (currentIndent < 0) {
            currentIndent = 0
        }
    }

    fun verify(status : QEDStatus, message: () -> Any? ) {
        super.info{ indentation + "Verify: $message" }
        val statusStr = when (status) {
            QEDStatus.FAIL -> "<u>(Failed)</u>"
            QEDStatus.PASS -> "<u>(Passed)</u>"
            QEDStatus.SKIP -> "<u>(Skipped)</u>"
            else -> ""
        }
        reportSinks.forEach { it.log(status, indentation + "<u><b>Verify</b></u>: ${message().toString()} $statusStr") }

    }


}




