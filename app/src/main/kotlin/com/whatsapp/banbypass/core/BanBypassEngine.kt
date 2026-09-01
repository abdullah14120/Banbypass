package com.whatsapp.banbypass.core

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.util.UUID
import javax.net.ssl.HttpsURLConnection
import javax.net.ssl.SSLSocketFactory

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
            val connection = url.openConnection() as HttpURLConnection
            
            if (connection is HttpsURLConnection) {
                connection.sslSocketFactory = SSLSocketFactory.getDefault() as SSLSocketFactory
            }

            connection.requestMethod = "POST"
            connection.setRequestProperty("User-Agent", "WhatsApp/2.6.21 Android/34")
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
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
        } catch (e: java.net.UnknownHostException) {
            Pair(-1, "DNS Resolution Failed: Check network connectivity or domain availability.")
        } catch (e: Exception) {
            Pair(-1, e.localizedMessage ?: "Network I/O exception")
        }
    }
}
