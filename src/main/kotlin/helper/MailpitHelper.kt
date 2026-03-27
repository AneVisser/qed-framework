package qed.helper

import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import org.json.JSONObject
import org.json.JSONArray

/**
 * Helper for querying and managing the Mailpit email capture server.
 * Mailpit runs on vm-mailpit and captures all outbound emails from
 * staging, preprod, and local dev (when using application-local-test.conf).
 *
 * API base: http://192.168.56.13:8025
 */
object MailpitHelper {

//    private const val MAILPIT_BASE_URL = "http://192.168.56.13:8025"
    private const val MAILPIT_BASE_URL = "http://192.168.50.104:8025"
    private val client = HttpClient.newHttpClient()

    /**
     * Delete all captured messages.
     * Call in @BeforeMethod for any test that sends email, to prevent
     * messages from a previous run interfering.
     */
    fun clearInbox() {
        val request = HttpRequest.newBuilder()
            .uri(URI.create("$MAILPIT_BASE_URL/api/v1/messages"))
            .DELETE()
            .build()
        client.send(request, HttpResponse.BodyHandlers.ofString())
    }

    /**
     * Wait for an email matching the given recipient and subject to arrive.
     * Polls every 500ms until the message appears or the timeout is reached.
     * Retrieves the full message body and deletes it by ID after retrieval
     * so concurrent test runs are not affected.
     *
     * @param toAddress  exact recipient email address to match
     * @param subjectContains  substring to match in the subject line
     * @param timeoutMs  how long to wait before failing (default 10 seconds)
     * @return the full message as a JSONObject (has "Text" and "HTML" fields)
     */
    fun waitForEmail(
        toAddress: String,
        subjectContains: String,
        timeoutMs: Int = 10000
    ): JSONObject {
        val deadline = System.currentTimeMillis() + timeoutMs
        while (System.currentTimeMillis() < deadline) {
            val listRequest = HttpRequest.newBuilder()
                .uri(URI.create("$MAILPIT_BASE_URL/api/v1/messages"))
                .GET()
                .build()
            val listResponse = client.send(listRequest, HttpResponse.BodyHandlers.ofString())
            val body = JSONObject(listResponse.body())
            val messages = body.optJSONArray("messages") ?: JSONArray()

            for (i in 0 until messages.length()) {
                val msg = messages.getJSONObject(i)
                val subject = msg.optString("Subject", "")
                val toArray = msg.optJSONArray("To") ?: JSONArray()
                val matchesRecipient = (0 until toArray.length()).any { j ->
                    toArray.getJSONObject(j).optString("Address", "") == toAddress
                }
                if (matchesRecipient && subject.contains(subjectContains)) {
                    val id = msg.getString("ID")
                    // Retrieve full message (contains Text and HTML body)
                    val fullMessage = getMessageById(id)
                    // Delete by ID — don't bulk clear in case other tests have messages
                    deleteMessageById(id)
                    return fullMessage
                }
            }
            Thread.sleep(500)
        }
        throw AssertionError(
            "No email to '$toAddress' with subject containing '$subjectContains' " +
                    "arrived within ${timeoutMs}ms"
        )
    }

    /**
     * Extract the first URL from a plain-text email body that contains the given path fragment.
     * Example: extractLink(email.get("Text").toString(), "/verify-email?token=")
     */
    fun extractLink(emailText: String, pathFragment: String): String {
        return emailText.lines()
            .flatMap { it.split(" ", "<", ">", "\"") }
            .firstOrNull { it.contains(pathFragment) && it.startsWith("http") }
            ?: throw AssertionError("No link containing '$pathFragment' found in email body")
    }

    // -------------------------------------------------------------------------

    private fun getMessageById(id: String): JSONObject {
        val request = HttpRequest.newBuilder()
            .uri(URI.create("$MAILPIT_BASE_URL/api/v1/message/$id"))
            .GET()
            .build()
        val response = client.send(request, HttpResponse.BodyHandlers.ofString())
        return JSONObject(response.body())
    }

    private fun deleteMessageById(id: String) {
        val request = HttpRequest.newBuilder()
            .uri(URI.create("$MAILPIT_BASE_URL/api/v1/message/$id"))
            .DELETE()
            .build()
        client.send(request, HttpResponse.BodyHandlers.ofString())
    }
}