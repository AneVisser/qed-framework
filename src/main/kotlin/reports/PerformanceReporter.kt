package qed.reports

import com.aventstack.extentreports.ExtentTest
import com.aventstack.extentreports.Status
import kotlinx.html.div
import kotlinx.html.span
import kotlinx.html.stream.createHTML
import kotlinx.html.style
import kotlinx.html.table
import kotlinx.html.tbody
import kotlinx.html.td
import kotlinx.html.th
import kotlinx.html.thead
import kotlinx.html.tr
import qed.json.QEDJson
import qed.json.QEDJson.moshi
import qed.performance.PerformanceTracker
import qed.performance.PerformanceTracker.getAllRecords
import qed.testbaseclass.IURLPath
import qed.testbaseclass.QEDDate
import qed.testbaseclass.RequestType
import qed.testbaseclass.TestRunContext
import qed.testbaseclass.coefVar
import qed.testbaseclass.standardDeviation
import java.io.File
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * Object to generate performance reports for all REST.send requests that had trackPerformance = true,
 * so that a collection of performance records was created
 */
object PerformanceReporter {

    data class StatsRecord(
        val endPoint : Pair<IURLPath, RequestType>?,        // null for pooled numbers
        val numObs : Int,
        val average : Double,
        val fastest : Double,
        val slowest : Double,
        val stddev : Double,
        val varCoef : Double)
    val statsRecs : MutableList<StatsRecord> = mutableListOf()
    data class ChartRecord(val endpoint : Pair<IURLPath, RequestType>?, val method : String, val duration : Double)
    val chartRecs  : MutableList<ChartRecord> = mutableListOf()

    private val htmlBuffer = StringBuilder()
    fun appendHTML(line: String) {
        htmlBuffer.appendLine(line)
    }

    lateinit var summaryTest : ExtentTest
    lateinit var historyTest : ExtentTest

    fun generate() {
        // don't do anything if there were no performance records created.
        if (!PerformanceTracker.hasRecords())
            return
        summaryTest = ExtentManager.createTest("Performance Summary")
        historyTest = ExtentManager.createTest("Performance History")
        renderSummary()
        createCharts()
        createCssCharts()
        addToPersistentData()
        reportTrends()
        flushToExtentReport()
    }

    private fun renderSummary() {
        val stats = PerformanceTracker.getStats()
        val statsByEndpoint = PerformanceTracker.getStatsByEndpoint()
        stats?.apply {
            statsRecs.add(StatsRecord(null, count, average, fastest, slowest, std, varCoeff))
        }
        statsByEndpoint.forEach { (endpoint, stat) ->
            statsRecs.add(StatsRecord(endpoint, stat.count, stat.average, stat.fastest, stat.slowest, stat.std, stat.varCoeff))
        }
    }

    fun createCharts() {
        val allDurations = getAllRecords()
            .flatMap { (_, records) ->
                records.map { it } // convert to ms
            }
        allDurations.forEach {
            val duration = it.endTime - it.startTime
            val method = it.methodName //.padEnd(methodWidth)
            val durationMs = duration / 1_000_000.0
            chartRecs.add(ChartRecord(Pair(it.urlPath, it.type), method, durationMs))
        }
    }

    private fun parseHTML() : String {
        val html = htmlBuffer.toString()
        htmlBuffer.clear()
        return html
    }


    private fun createSummary(key : Pair<IURLPath, RequestType>?, data : StatsRecord, tableClasses : String) : String {
        return createHTML().table(classes = tableClasses){
            thead {
                tr {
                    if (key==null) {
                        th(classes = "col-endpoint") { +"Overall" }
                        th(classes = "col-reqtype") { +" " }
                    }
                    else {
                        th(classes = "col-endpoint") { +"Endpoint" }
                        th(classes = "col-reqtype") { +"Request type" }
                    }
                    th(classes="col-numobs") {+"Obs"}
                    th(classes="col-avg") {+"Avg"}
                    th(classes="col-fastest") {+"Fastest"}
                    th(classes="col-slowest") {+"Slowest"}
                    th(classes="col-std") {+"Std"}
                    th(classes="col-vc") {+"CV"}
                }
            }
            tbody {
                tr {
                    td(classes="col-endpoint") {+"${key?.first ?: " "}"}
                    td(classes="col-reqtype") {+"${key?.second ?: " "}"}
                    td(classes="col-numobs") {+"${data.numObs}"}
                    td(classes="col-avg") {+"%.2f ms".format(data.average)}
                    td(classes="col-fastest") {+"%.2f ms".format(data.fastest)}
                    td(classes="col-slowest") {+"%.2f ms".format(data.slowest)}
                    td(classes="col-std") {+"%.2f ms".format(data.stddev)}
                    td(classes="col-vc") {+"%.1f".format(data.varCoef)}
                }
            }
        }
    }

    fun createCssCharts() {
        val aggregated = statsRecs.first {it.endPoint==null}

        val overall = createSummary(null, aggregated, "perf-table")
        appendHTML(overall)

        // add sub-table with individual records
        val maxDuration = chartRecs.maxOf { it.duration }

        chartRecs.groupBy { it.endpoint }.forEach {chartGroup ->
            val key = chartGroup.key
            val stat = statsRecs.find{it.endPoint?.first == key!!.first && it.endPoint.second == key.second}!!
            val summary = createSummary(key, stat, "perf-table perf-summary")
            appendHTML(summary)

            val details = createHTML().table(classes = "perf-table perf-detail") {
                thead {
                    tr {
                        th(classes = "col-method") { +"Method"}
                        th(classes = "col-duration") { +"Duration"}
                        th(classes = "col-timeline") { +"Timeline"}
                    }
                }
                chartGroup.value.forEach {
                    val percent = (it.duration / maxDuration * 96).coerceAtMost(96.0)
                    tbody {
                        tr {
                            td("col-method") { +it.method }
                            td("col-duration") { +"%.2f ms".format(it.duration)}
                            td("col-timeline") {
                                span(classes = "perf-bar") {
                                    div(classes = "bar-container") {
                                        div(classes = "bar-fill") {
                                            style = "width: %.2f%%".format(percent)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            appendHTML(details)
        }

        val table = parseHTML()
        summaryTest.log(Status.INFO, table)
        htmlBuffer.clear();
    }

    private fun flushToExtentReport() = ExtentManager.flush()

/******************************************** Performance Overview  ***************************************************/
    /**
     * Code below is used to save persistent data, e.g. save the data from the last run to a file,
     * and report on trends.
     */
    data class PerformanceRec(
        val urlpath : String,
        val requestType : RequestType,
        val duration : Double
    )
    data class PerfSnapshot(
        val metadata: EnvironmentInfo,
        val records: List<PerformanceRec>
    )
    data class CommitData(val branch : String, val commit : String, val timestamp : String)

    data class EnvironmentInfo(
        val commit : CommitData,
        val environment : String,
        val sut : String,
        val rundate : String
    )

    fun toJsonSnapshot(metadata: EnvironmentInfo, records: List<PerformanceRec>): String {
        val adapter = moshi.adapter(PerfSnapshot::class.java).indent("  ")
        val wrapper = PerfSnapshot(metadata, records)
        return adapter.toJson(wrapper)
    }

    fun fromJsonSnapshot(json: String): PerfSnapshot? {
        val adapter = moshi.adapter(PerfSnapshot::class.java)
        return adapter.fromJson(json)
    }

    fun addToPersistentData() {
        val records = getAllRecords()
            .flatMap { (_, records) ->
                records.map {it }
            }
        val recList = mutableListOf<PerformanceRec>()
        records.forEach {
            recList.add(PerformanceRec(it.urlPath.route, it.type , it.duration!!, ))
        }

        // setup the data that need to be reported in the json run file
        val runDateTime = QEDDate("dd-MMM-yyyy_HH-mm-ss").toString()
        val runDateTimeStamp = QEDDate("yyyy-MM-dd HH:mm:ss").toString()
        val configmetadata = TestRunContext.testrunmetadata
        val commit = getSUTCommitName(configmetadata?.repository!!)
        val env = System.getProperty("env.name", "dev")
        val envinfo = EnvironmentInfo(commit, env, configmetadata.sut!!, runDateTimeStamp)
        val jsonList = toJsonSnapshot(envinfo, recList)

        val runId = "rpt_$runDateTime.json"
        val rootDir = File("perf-history", configmetadata.sut)                      // platform-safe
        val file = File(File(rootDir, commit.commit), runId)         // platform-safe
        file.parentFile.mkdirs()
        file.writeText(jsonList)
        enforceRetentionPolicy(rootDir, configmetadata.maxRunsPerCommit!!, configmetadata.maxCommitsToKeep!!)
    }

    private fun enforceRetentionPolicy(root: File, maxCommits: Int, maxRunsPerCommit: Int) {
        val commitDirs = root.listFiles()?.filter { it.isDirectory }?.sortedBy { it.lastModified() } ?: return
        // Trim commits
        if (commitDirs.size > maxCommits) {
            commitDirs.take(commitDirs.size - maxCommits).forEach { it.deleteRecursively() }
        }
        // Trim runs per commit
        commitDirs.forEach { dir ->
            val runs = dir.listFiles()?.filter { it.name.endsWith(".json") && it.name != "latest.json" }
                ?.sortedBy { it.lastModified() } ?: return@forEach
            if (runs.size > maxRunsPerCommit) {
                runs.take(runs.size - maxRunsPerCommit).forEach { it.delete() }
            }
        }
    }


    private fun getSUTBranchName(repoPath : String): String {
        try {
            val process = ProcessBuilder("git", "rev-parse", "--abbrev-ref", "HEAD")
                .directory(File(repoPath))
                .start()
            return process.inputStream.bufferedReader().readText().trim()
        } catch(e : Exception) {
            historyTest.fail {e.message}
            historyTest.info {"set testrunmetadata | repository in your config file to the correct .git directory" }
        }
        return ""
    }

    private fun getSUTCommitHash(repoPath : String): String {
        try {
            val process = ProcessBuilder("git", "rev-parse", "HEAD")
                .directory(File(repoPath)) // adjust this path
                .start()
            return process.inputStream.bufferedReader().readText().trim()
        } catch(e : Exception) {
            historyTest.fail {e.message}
            historyTest.info {"set testrunmetadata | repository in your config file to the correct .git directory" }
        }
        return ""
    }

    private fun getCommitTimestamp(repoPath: String, commitHash: String = "HEAD"): String {
        try {
            val process = ProcessBuilder("git", "show", "-s", "--format=%ct", commitHash)
                .directory(File(repoPath))
                .start()
            val epochSeconds = process.inputStream.bufferedReader().readText().trim().toLong()
            val instant = Instant.ofEpochSecond(epochSeconds)
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.systemDefault())
            return formatter.format(instant)
        } catch(e : Exception) {
            historyTest.fail {e.message}
            historyTest.info {"set testrunmetadata | repository in your config file to the correct .git directory" }
        }
        return ""
    }



    // this function is a generic one that retrieves the commit number of a system under test.
    // for now, it is replaced with a repo to C:\Projects\delphitools

    private fun getSUTCommitName(repoPath : String) = CommitData(
        getSUTBranchName(repoPath),
        getSUTCommitHash(repoPath),
        getCommitTimestamp(repoPath))

    /**
     * The functions below are for reporting trends in performance
     */

    data class AggregatedStats(
        val endpoint: String,
        val requestType: RequestType,
        val stddev: Double,
        val coefVar : Double,
        val mean : Double,
        val durations: List<Double>
    )


    fun reportTrends() {
        val configmetadata = TestRunContext.testrunmetadata
        val rootDir = File("perf-history", configmetadata?.sut!!)         // platform-safe
        val snapshotList = loadAllSnapshots(rootDir)
        val stats = aggregateStats(snapshotList)
        renderMarkdownChart(stats)

        val commitStats = aggregateByCommit(snapshotList)
        renderTimelineChart(commitStats, stats)
        val html = parseHTML()

        historyTest.log(Status.INFO, html)
    }

    private fun loadAllSnapshots(sutDir: File): List<PerfSnapshot> {
        return sutDir.listFiles()
            ?.filter { it.isDirectory }
            ?.flatMap { commitDir ->
                commitDir.listFiles()
                    ?.filter { it.name.endsWith(".json") && it.name != "latest.json" }
                    ?.mapNotNull { file ->
                        val json = file.readText()
                        QEDJson.fromJson<PerfSnapshot>(json)
                    } ?: emptyList()
            } ?: emptyList()
    }



    fun aggregateStats(snapshots: List<PerfSnapshot>): List<AggregatedStats> {
            return snapshots.flatMap { it.records }
            .groupBy { it.urlpath to it.requestType }
            .map { (key, records) ->
                val durations = records.map { it.duration }
                AggregatedStats(key.first, key.second,
                    durations.standardDeviation(),
                    durations.coefVar(),
                    durations.average(),
                    durations)
            }
    }


    fun renderMarkdownChart(stats: List<AggregatedStats>) {
        val maxDuration = stats.maxOf { it.durations.average() }
        val html = createHTML().table(classes = "perf-table") {
            thead {
                tr {
                    th(classes = "col-method") { +"Endpoint" }
                    th(classes = "col-duration") { +"Request type" }
                    th(classes = "col-duration") { +"Avg duration" }
                    th(classes = "col-timeline") { +"Chart" }
                }
            }
            tbody {
                stats.sortedByDescending { it.durations.average() }.forEach { stat ->
                    val avg = stat.durations.average()
                    val percent = (avg / maxDuration * 96).coerceAtMost(96.0)
                    tr {
                        td { +stat.endpoint }
                        td { +"${stat.requestType}" }
                        td { +"%.2f".format(avg)}
                        td("col-timeline") {
                            span(classes = "perf-bar") {
                                div(classes = "bar-container") {
                                    div(classes = "bar-fill") {
                                        style = "width: %.2f%%".format(percent)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        appendHTML(html)
    }


    data class CommitPerf(
        val commit: String,
        val timestamp: String,
        val rundate : String,
        val endpoint: String,
        val requestType: RequestType,
        val avgDuration: Double
    )

    fun aggregateByCommit(snapshots: List<PerfSnapshot>): List<CommitPerf> {
        return snapshots.flatMap { snapshot ->
            snapshot.records
                .groupBy { it.urlpath to it.requestType }
                .map { (key, records) ->
                    CommitPerf(
                        commit = snapshot.metadata.commit.commit,
                        timestamp = snapshot.metadata.commit.timestamp,
                        rundate = snapshot.metadata.rundate,
                        endpoint = key.first,
                        requestType = key.second,
                        avgDuration = records.map { it.duration }.average()
                    )
                }
        }
    }

    fun renderTimelineChart(perfs: List<CommitPerf>, stats : List<AggregatedStats>) {
        val maxDuration = perfs.maxOf { it.avgDuration }
        val grouped = perfs.groupBy { it.endpoint to it.requestType }

        grouped.forEach { (key, entries) ->
            val html = createHTML().table(classes = "perf-table perf-summary") {
                thead {
                    tr {
                        th(classes = "col-method") { +"Endpoint" }
                        th(classes = "col-duration") { +"Request type" }
                        th(classes = "col-duration") { +"Avg duration" }
                        th(classes = "col-duration") {  }
                        th(classes = "col-duration") {  }
                        th(classes = "col-timeline") {  }
                    }
                }
                tbody {
                    tr {
                        th(classes = "col-method") { +key.first }
                        th(classes = "col-duration") { +"${key.second}" }
                        th(classes = "col-duration") { +"%.2f".format(entries.map{it.avgDuration}.average()) }
                        th(classes = "col-duration") {  }
                        th(classes = "col-duration") {  }
                        th(classes = "col-timeline") {  }
                    }
                }
            }
            appendHTML(html)
            val details = createHTML().table(classes = "perf-table perf-detail") {
                thead {
                    tr {
                        th(classes = "col-commit") { +"Commit" }
                        th(classes = "col-timestamp") { +"Time stamp" }
                        th(classes = "col-timestamp") { +"Run date" }
                        th(classes = "col-duration") { +"Avg duration" }
                        th(classes = "col-timeline") { +"Chart" }
                    }
                }
                tbody {
                    entries.sortedBy { it.timestamp }.forEach { perf ->
                        val percent = (perf.avgDuration / maxDuration * 96).coerceAtMost(96.0)
                        // find the record in aggregated stats for stdev and mean
                        val statsRec = stats.find{it.endpoint == perf.endpoint && it.requestType == perf.requestType}!!
                        val deviation = (perf.avgDuration - statsRec.mean)
                        val deviationRatio = deviation.div(statsRec.stddev)
                        val colour = when {
                            statsRec.coefVar <= 0.1 && deviationRatio <= 1.0 -> "perf-fast"         // stable
                            statsRec.coefVar <= 0.1 && deviationRatio > 1.0 -> "perf-slow"          // possibly regression
                            statsRec.coefVar > 0.1 && deviationRatio <= 2.0 -> "perf-medium"        // noisy; high variation
                            else -> "perf-slow"                                                     // likely regression
                        }
                        tr {
                            td { +"${perf.commit.take(7)}..." }
                            td { +perf.timestamp }
                            td { +perf.rundate }  // run date
                            td { +"%.2f ms".format(perf.avgDuration) }
                            td("col-timeline") {
                                span(classes = "perf-bar") {
                                    div(classes = "bar-container") {
                                        div(classes = "bar-fill $colour") {
                                            style = "width: %.2f%%".format(percent)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            appendHTML(details)
        }
    }


}




