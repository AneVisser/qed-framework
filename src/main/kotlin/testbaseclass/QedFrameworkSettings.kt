package qed.testbaseclass

import QedFrameworkConfig
import QedFrameworkEnvironment
import qed.json.QEDJson
import java.io.File

/**
 * Singleton that loads and exposes the QED framework-level config.
 * Config file location: ~/.qed/qed-framework.json
 *
 * Loaded once on first access. The environment is selected using the
 * same env.name system property as the SUT config (default: "dev").
 *
 * Example config file:
 * {
 *   "environments": {
 *     "dev":    { "mailpitBaseUrl": "http://192.168.50.104:8025" },
 *     "stag":   { "mailpitBaseUrl": "http://192.168.56.13:8025" },
 *     "preprod":{ "mailpitBaseUrl": "http://192.168.56.13:8025" }
 *   }
 * }
 */
object QedFrameworkSettings {

    private val settings: QedFrameworkEnvironment? by lazy { load() }

    /** Base URL for the Mailpit SMTP capture server, e.g. http://192.168.56.13:8025 */
    val mailpitBaseUrl: String
        get() = settings?.mailpitBaseUrl
            ?: "http://192.168.56.13:8025"   // sensible default if config missing

    // -------------------------------------------------------------------------

    private fun load(): QedFrameworkEnvironment? {
        val resource = QedFrameworkSettings::class.java
            .getResourceAsStream("/qed-framework.json")
            ?: run {
                println("⚠️  QED framework config not found at classpath:/qed-framework.json — using defaults")
                return null
            }
        return try {
            val env = System.getProperty("env.name", "dev")
            val config = QEDJson.fromJson<QedFrameworkConfig>(resource.bufferedReader().readText())
            config?.environments?.get(env)
        } catch (e: Exception) {
            println("⚠️  Failed to load QED framework config: ${e.message} — using defaults")
            null
        }
    }

}