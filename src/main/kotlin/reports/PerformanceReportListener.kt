package qed.reports

import QedConfig
import org.testng.IExecutionListener
import org.testng.ISuite
import org.testng.ISuiteListener
import qed.json.QEDJson
import qed.performance.PerformanceTracker
import qed.testbaseclass.TestRunContext.testrunmetadata
import java.io.File

class PerformanceReportListener : IExecutionListener, ISuiteListener {

    /**
     * When the execution of a test suite has finished, a performance report
     * is generated if there have been any REST send requests executed with the parameter
     * trackPerformance = true
     */
    override fun onExecutionFinish() {
        if (PerformanceTracker.hasRecords()) {
            PerformanceReporter.generate()
        }
    }

    /**
     * When starting the test
     */
    override fun onStart(suite: ISuite?) {
        super.onStart(suite)
        val configfile = suite?.xmlSuite?.parameters?.get("configfile")
        testrunmetadata = QEDJson.fromJson<QedConfig>(File(configfile!!).readText())?.testrunmetadata
    }
}
