/**
 * Data model for the QED framework-level config file.
 * Lives at ~/.qed/qed-framework.json
 * Contains infrastructure settings that apply across all SUTs.
 */
data class QedFrameworkConfig(
    val environments: Map<String, QedFrameworkEnvironment>? = null
)

data class QedFrameworkEnvironment(
    val mailpitBaseUrl: String? = null
)