package qed.performance

import qed.json.QEDJson
import qed.testbaseclass.IURLPath
import qed.testbaseclass.RequestType
import qed.testbaseclass.standardDeviation
import java.io.File
import java.util.concurrent.ConcurrentHashMap

data class PerformanceStats(
    val count : Int,
    val average : Double,
    val fastest : Double,
    val slowest : Double,
    val std : Double,
    val varCoeff : Double,
)

object PerformanceTracker {
    private val records = ConcurrentHashMap<Long, MutableList<PerfRecord>>()

    data class PerfRecord(
        val label: String,
        val methodName: String,
        val type : RequestType,
        val urlPath: IURLPath,
        val startTime: Long,
        val endTime: Long,
        val duration: Double?
    )

    fun record(label: String, methodName : String, requestType : RequestType, urlPath : IURLPath, start: Long, end: Long) {
        val threadId = Thread.currentThread().threadId()
        val record = PerfRecord(label, methodName, requestType, urlPath, start, end, (end - start) / 1_000_000.0)
        // checks if the key exists, and if not, inserts a new mutable list
        records.computeIfAbsent(threadId) { mutableListOf() }.add(record)
    }

    fun hasRecords() = records.size > 0

    fun getAllRecords(): Map<Long, List<PerfRecord>> = records

    fun dumpAll() : List<String> {
        val returnValue = mutableListOf<String>()
        getAllRecords().forEach { (threadId, records) ->
            records.forEach { record ->
                val durationMs = (record.endTime - record.startTime) / 1_000_000
                returnValue.add("Thread $threadId | ${record.methodName} (${record.type}) ${record.urlPath} took ${durationMs}ms" )
            }
        }
        return returnValue
    }

    fun getStats() : PerformanceStats? {
        val allDurations = getAllRecords()
            .flatMap { (_, records) ->
                records.map { (it.endTime - it.startTime) / 1_000_000.0 } // convert to ms
            }
        if (allDurations.isEmpty()) {
            return null
        }
        val count = allDurations.size
        val average = allDurations.average()
        val max = allDurations.maxOrNull() ?: 0.0
        val min = allDurations.minOrNull() ?: 0.0
        val stdDev = kotlin.math.sqrt(
            allDurations.map { (it - average) * (it - average) }.average()
        )
        val variationCoefficient = if (average != 0.0) stdDev / average else 0.0
        return PerformanceStats(count, average, min, max, stdDev, variationCoefficient)
    }

    fun getStatsByEndpoint(): Map<Pair<IURLPath, RequestType>, PerformanceStats> {
        val grouped = getAllRecords()
            .flatMap { it.value } // flatten all records
            .groupBy { Pair(it.urlPath, it.type) } // group by endpoint + method

        return grouped.mapValues { (_, records) ->
            val durations = records.map { (it.endTime - it.startTime) / 1_000_000.0 }
            val count = durations.size
            val average = durations.average()
            val max = durations.maxOrNull() ?: 0.0
            val min = durations.minOrNull() ?: 0.0
            val stdDev =durations.standardDeviation()
            val variationCoefficient = if (average != 0.0) stdDev / average else 0.0
            PerformanceStats(count, average, min, max, stdDev, variationCoefficient)
        }
    }



}

// this function is used as a wrapper around the send method of RESTClient,
// so that for each call a performance record is created
inline fun <T> trackPerf(uniqueId: String, methodName : String, requestType: RequestType, urlPath : IURLPath, block: () -> T): T {
    val start = System.nanoTime()
    try {
        return block()
    } finally {
        val end = System.nanoTime()
        PerformanceTracker.record(uniqueId, methodName, requestType, urlPath, start, end)
    }
}


data class EndpointStats(
    val urlPath: String,
    val requestType: String,
    val stats: PerformanceStats
)

// write performance data into json file; the uniqueness of the name is the responsibility of the caller
fun persistPerformanceData(runId: String) {
    val statsByEndpoint = PerformanceTracker.getStatsByEndpoint()

    val serializableStats = statsByEndpoint.map { (key, stats) ->
        EndpointStats(
            urlPath = key.first.toString(), // or key.first.name if it's an enum
            requestType = key.second.name,
            stats = stats
        )
    }

    val json = QEDJson.toJson(serializableStats)
    val storageDir = System.getProperty("user.dir") + "/perf-history"
    val file = File("$storageDir/$runId.json")
    file.parentFile.mkdirs()
    file.writeText(json)
}

