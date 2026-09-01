package com.whatsapp.banbypass.core

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.OutputStreamWriter
import java.net.URL
import java.util.UUID
import javax.net.ssl.HttpsURLConnection
import javax.net.ssl.SSLContext

class BanBypassEngine(private val context: Context) {

    private fun generateRandomDeviceFingerprint(): String {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 16)
    }

    suspend fun executeAppealAutomation(fullPhoneNumber: String, authToken: String): Pair<Int, String> = withContext(Dispatchers.IO) {
        try {
            val sanitizedPhone = fullPhoneNumber.replace(Regex("[^0-9]"), "")
            if (sanitizedPhone.length < 8) {
                return@withContext Pair(400, "Invalid phone number length")
            }

            val countryCode = sanitizedPhone.take(3)
            val subscriberNumber = sanitizedPhone.drop(3)

            val url = URL("https://g.whatsapp.net/v2/register")
            val connection = url.openConnection() as HttpsURLConnection

            // فرض استخدام TLS 1.2 أو أحدث لتجنب Connection Reset أثناء الـ Handshake
            val sslContext = SSLContext.getInstance("TLSv1.3").apply {
                init(null, null, null)
            }
            connection.sslSocketFactory = sslContext.socketFactory

            connection.requestMethod = "POST"
            connection.setRequestProperty("User-Agent", "WhatsApp/2.24.0.0 Android/34")
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
            connection.setRequestProperty("Accept", "text/json")
            connection.connectTimeout = 15000
            connection.readTimeout = 15000
            connection.doOutput = true

            val deviceId = generateRandomDeviceFingerprint()
            val payload = "cc=$countryCode&in=$subscriberNumber&mistyped=0&auth_token=$authToken&debug_id=$deviceId"

            OutputStreamWriter(connection.outputStream).use { writer ->
                writer.write(payload)
                writer.flush()
            }

            val responseCode = connection.responseCode
            val responseStream = if (responseCode in 200..299) {
                connection.inputStream
            } else {
                connection.errorStream
            }

            val responseBody = responseStream?.bufferedReader().use { it?.readText() ?: "" }
            Pair(responseCode, responseBody)
        } catch (e: Exception) {
            Pair(-1, "Connection Reset / Handshake Failed: ${e.localizedMessage}")
        }
    }
}
