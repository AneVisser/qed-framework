data class QedConfig(
    val environments: Map<String, Map<String, String>>? = null,
    val widgettype: String? = null,
    val browser: String? = null,
    val reporting: ReportingConfig? = null,
    val testrunmetadata : TestRunMetaData? = null
)

data class ReportingConfig(
    val extent: ExtentConfig? = null
)

data class ExtentConfig(
    val theme: String? = null,
    val reportName : String? = null,
    val documentTitle : String? = null,
    val enableScreenshots: Boolean? = null
)
data class TestRunMetaData(
    val repository : String?,
    val sut : String?,
    val maxRunsPerCommit : Int?,
    val maxCommitsToKeep : Int?
)