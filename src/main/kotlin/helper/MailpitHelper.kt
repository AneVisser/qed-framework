package qed.helper

import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import org.json.JSONObject
import org.json.JSONArray

/**
 * Helper for querying and managing the Mailpit email capture server.
 * Mailpit captures all outbound emails from staging, preprod, and local dev.
 * Instantiated by TestContext after config is loaded.
 */
class MailpitHelper(private val mailpitBaseUrl: String) {

    private val client = HttpClient.newHttpClient()

    /**
     * Delete all captured messages.
     * Called in @BeforeSuite to prevent stale emails from prior runs interfering.
     */
    fun clearInbox() {
        val request = HttpRequest.newBuilder()
            .uri(URI.create("$mailpitBaseUrl/api/v1/messages"))
            .DELETE()
            .build()
        client.send(request, HttpResponse.BodyHandlers.ofString())
    }

    /**
     * Wait for an email matching the given recipient and subject to arrive.
     * Polls every 500ms until the message appears or the timeout is reached.
     * Retrieves the full message body and deletes it after retrieval
     * so concurrent test runs are not affected.
     */
    fun waitForEmail(
        toAddress: String,
        subjectContains: String,
        timeoutMs: Int = 15000
    ): JSONObject {
        val deadline = System.currentTimeMillis() + timeoutMs
        while (System.currentTimeMillis() < deadline) {
            val listRequest = HttpRequest.newBuilder()
                .uri(URI.create("$mailpitBaseUrl/api/v1/messages"))
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
                    val fullMessage = getMessageById(id)
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
     * Convenience method: wait for an email and extract a link containing the given path fragment.
     */
    fun getConfirmationLink(
        toAddress: String,
        subjectContains: String,
        pathFragment: String,
        timeoutMs: Int = 15000
    ): String {
        val email = waitForEmail(toAddress, subjectContains, timeoutMs)
        return extractLink(email.getString("Text"), pathFragment)
    }

    /**
     * Extract the first URL from a plain-text email body containing the given path fragment.
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
            .uri(URI.create("$mailpitBaseUrl/api/v1/message/$id"))
            .GET()
            .build()
        val response = client.send(request, HttpResponse.BodyHandlers.ofString())
        return JSONObject(response.body())
    }

    private fun deleteMessageById(id: String) {
        val request = HttpRequest.newBuilder()
            .uri(URI.create("$mailpitBaseUrl/api/v1/message/$id"))
            .DELETE()
            .build()
        client.send(request, HttpResponse.BodyHandlers.ofString())
    }
}