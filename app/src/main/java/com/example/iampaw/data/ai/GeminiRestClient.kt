package com.example.iampaw.data.ai

import android.util.Base64
import android.util.Log
import com.example.iampaw.data.local.GeminiImagePayload
import com.google.gson.Gson
import com.google.gson.JsonObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Cliente HTTP nativo de Gemini (REST). Evita el SDK Android + gRPC que falla con keys AQ.
 * y con Firestore en el mismo proyecto.
 */
@Singleton
class GeminiRestClient @Inject constructor() {

    private val gson = Gson()

    private val http = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(90, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    suspend fun generateContent(
        modelName: String,
        apiKey: String,
        prompt: String,
        image: GeminiImagePayload?
    ): String = withContext(Dispatchers.IO) {
        val url =
            "https://generativelanguage.googleapis.com/v1beta/models/$modelName:generateContent"
        val bodyJson = buildRequestJson(prompt, image)
        val request = Request.Builder()
            .url(url)
            .addHeader("Content-Type", "application/json")
            .addHeader("x-goog-api-key", apiKey)
            .post(bodyJson.toRequestBody(JSON_MEDIA_TYPE))
            .build()

        http.newCall(request).execute().use { response ->
            val rawBody = response.body?.string().orEmpty()
            if (!response.isSuccessful) {
                val apiMessage = extractApiErrorMessage(rawBody)
                throw IOException(
                    "HTTP ${response.code} $modelName: $apiMessage"
                )
            }
            extractModelText(rawBody)
        }
    }

    private fun buildRequestJson(prompt: String, image: GeminiImagePayload?): String {
        val parts = mutableListOf<JsonObject>()
        image?.takeIf { it.bytes.isNotEmpty() }?.let { payload ->
            parts += JsonObject().apply {
                add(
                    "inline_data",
                    JsonObject().apply {
                        addProperty("mime_type", payload.mimeType)
                        addProperty(
                            "data",
                            Base64.encodeToString(payload.bytes, Base64.NO_WRAP)
                        )
                    }
                )
            }
        }
        parts += JsonObject().apply { addProperty("text", prompt) }

        val root = JsonObject().apply {
            add(
                "contents",
                gson.toJsonTree(
                    listOf(
                        JsonObject().apply { add("parts", gson.toJsonTree(parts)) }
                    )
                )
            )
            add(
                "generationConfig",
                JsonObject().apply {
                    addProperty("responseMimeType", "application/json")
                }
            )
        }
        return gson.toJson(root)
    }

    private fun extractApiErrorMessage(rawBody: String): String {
        return try {
            val json = gson.fromJson(rawBody, JsonObject::class.java)
            json.getAsJsonObject("error")?.get("message")?.asString ?: rawBody.take(200)
        } catch (_: Exception) {
            rawBody.take(200)
        }
    }

    private fun extractModelText(rawBody: String): String {
        val json = gson.fromJson(rawBody, JsonObject::class.java)
        val error = json.getAsJsonObject("error")
        if (error != null) {
            throw IOException(error.get("message")?.asString ?: "Error de Gemini")
        }
        val parts = json.getAsJsonArray("candidates")
            ?.firstOrNull()
            ?.asJsonObject
            ?.getAsJsonObject("content")
            ?.getAsJsonArray("parts")
            ?: throw IOException("Gemini devolvió respuesta vacía")

        val text = buildString {
            parts.forEach { part ->
                val element = part.asJsonObject
                element.get("text")?.asString?.let { append(it) }
            }
        }.trim()

        if (text.isEmpty()) {
            Log.w(TAG, "Respuesta sin texto: ${rawBody.take(300)}")
            throw IOException("Gemini no devolvió texto en la respuesta")
        }
        return text
    }

    companion object {
        private const val TAG = "GeminiRestClient"
        private val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()
    }
}
