package com.whatsapp.banbypass.core

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.UUID
import java.util.concurrent.TimeUnit

class BanBypassEngine(private val context: Context) {

    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .hostnameVerifier { _, _ -> true }
        .build()

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
            val deviceId = generateRandomDeviceFingerprint()

            val payload = "cc=$countryCode&in=$subscriberNumber&mistyped=0&auth_token=$authToken&debug_id=$deviceId"
            val mediaType = "application/x-www-form-urlencoded; charset=utf-8".toMediaType()
            val body = payload.toRequestBody(mediaType)

            val request = Request.Builder()
                .url("https://g.whatsapp.net/v2/register")
                .header("User-Agent", "WhatsApp/2.24.0.0 Android/34")
                .header("Host", "g.whatsapp.net")
                .post(body)
                .build()

            client.newCall(request).execute().use { response ->
                val responseBody = response.body?.string() ?: ""
                Pair(response.code, responseBody)
            }
        } catch (e: Exception) {
            Pair(-1, "Network Exception: ${e.localizedMessage ?: "Unknown error"}")
        }
    }
}
