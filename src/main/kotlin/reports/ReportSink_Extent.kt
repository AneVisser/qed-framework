package qed.reports

import com.aventstack.extentreports.Status

class ReportSink_Extent : IReportSink {
    override val supportsHtml = true
    override fun log(status: QEDStatus, message: String) {
        ExtentManager.getTest()?.log(status.toExtentStatus(), cleanMessage(message))
    }
}

// Extension function to convert a QED Status to an extent reports Status
fun QEDStatus.toExtentStatus(): Status = when (this) {
    QEDStatus.INFO, QEDStatus.DEBUG, QEDStatus.TRACE -> Status.INFO
    QEDStatus.WARN -> Status.WARNING
    QEDStatus.ERROR, QEDStatus.FAIL -> Status.FAIL
    QEDStatus.PASS -> Status.PASS
    QEDStatus.SKIP -> Status.SKIP
    QEDStatus.VERIFY -> Status.INFO
}
