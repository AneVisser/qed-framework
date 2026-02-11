package qed.reports

/**
 * The idea here is to separate logging core (indentation, formatting, delegation to KLogger) from report sinks
 * (Extent or other reporting tools). That way, you can enable/disable sinks at runtime without changing the Logger itself.
 */

interface IReportSink {
    val supportsHtml: Boolean
    fun log(status: QEDStatus, message: String)

    // in any implementation of function 'log', use cleanMessage(message), so that the setting of supportsHTML determines
    // if html should be stripped or not.
    fun cleanMessage(message : String) =
        when (supportsHtml) {
            true -> message
            false -> HtmlUtils.stripTags(message)
        }

}


enum class QEDStatus {
    INFO,
    DEBUG,
    TRACE,
    WARN,
    ERROR,
    PASS,
    FAIL,
    SKIP,
    VERIFY
}


object HtmlUtils {
    private val TAG_REGEX = "<[^>]*>".toRegex()
    fun stripTags(input: String): String = input.replace(TAG_REGEX, "")
}