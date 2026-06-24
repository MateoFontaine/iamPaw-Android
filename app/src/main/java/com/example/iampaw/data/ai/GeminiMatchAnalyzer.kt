package com.example.iampaw.data.ai

import android.util.Log
import com.example.iampaw.BuildConfig
import com.example.iampaw.components.feed.DogPost
import com.example.iampaw.components.report.ReportDraft
import com.example.iampaw.data.local.GeminiImagePayload
import kotlinx.coroutines.delay
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GeminiMatchAnalyzer @Inject constructor(
    private val restClient: GeminiRestClient
) {

    suspend fun analyzeReport(
        draft: ReportDraft,
        candidates: List<DogPost>,
        image: GeminiImagePayload?
    ): Result<GeminiMatchResult> {
        val apiKey = BuildConfig.GEMINI_API_KEY.trim()
        if (apiKey.isBlank()) {
            Log.w(TAG, "GEMINI_API_KEY vacía — agregala en local.properties y hacé Sync Gradle")
            return Result.failure(IllegalStateException("API key de Gemini no configurada"))
        }

        val preparedImage = image?.let { GeminiImagePreparer.prepare(it.bytes, it.mimeType) }
        val prompt = buildPrompt(draft, candidates)
        val triedModels = mutableListOf<String>()
        var lastError: Exception? = null

        for (modelName in MODEL_FALLBACK_CHAIN) {
            repeat(MAX_ATTEMPTS_PER_MODEL) { attempt ->
                if (attempt > 0) delay(RETRY_DELAY_MS)
                triedModels += modelName
                try {
                    Log.d(
                        TAG,
                        "REST $modelName intento ${attempt + 1}/$MAX_ATTEMPTS_PER_MODEL, " +
                            "img=${preparedImage?.bytes?.size ?: 0} bytes"
                    )
                    val rawText = restClient.generateContent(
                        modelName = modelName,
                        apiKey = apiKey,
                        prompt = prompt,
                        image = preparedImage
                    )
                    Log.d(TAG, "OK con $modelName (${rawText.length} chars)")
                    val parseResult = GeminiResponseParser.parse(rawText)
                    if (parseResult.isSuccess) return parseResult
                    lastError = parseResult.exceptionOrNull() as? Exception
                        ?: IllegalStateException("No se pudo parsear la respuesta de Gemini")
                    Log.w(TAG, "Parse falló con $modelName: ${lastError?.message}")
                } catch (e: Exception) {
                    lastError = e
                    Log.w(TAG, "$modelName falló: ${e.message}")
                    if (!GeminiErrorMapper.isRetryable(e) && !isModelNotFound(e)) {
                        break
                    }
                }
            }
        }

        val error = lastError ?: IllegalStateException("Error desconocido de Gemini")
        val detail = "${GeminiErrorMapper.toUserMessage(error)} Modelos probados: ${triedModels.distinct().joinToString()}"
        return Result.failure(IllegalStateException(detail, error))
    }

    private fun isModelNotFound(error: Exception): Boolean {
        val raw = error.message.orEmpty()
        return raw.contains("404") || raw.contains("not found", ignoreCase = true)
    }

    private fun buildPrompt(draft: ReportDraft, candidates: List<DogPost>): String {
        val candidatesBlock = if (candidates.isEmpty()) {
            "(No hay reportes candidatos en la base de datos)"
        } else {
            candidates.joinToString(separator = "\n") { post ->
                "- id=${post.id}, nombre=${post.name}, raza=${post.breed}, " +
                    "ubicación=${post.location}, estado=${post.status}, " +
                    "color=${post.color}, detalles=${post.details}"
            }
        }

        return """
            Sos un asistente de mascotas perdidas en Argentina.
            El usuario reportó:
            - nombre: ${draft.name}
            - raza: ${draft.breed}
            - color: ${draft.colorText}
            - tamaño: ${draft.sizeText}
            - ubicación: ${draft.location}
            - detalles: ${draft.detailsText}
            - estado: ${draft.status}

            Analizá la imagen adjunta (si hay) y compará con estos reportes existentes:
            $candidatesBlock

            Criterios para matchPercentage (mismo perro = score alto):
            - 85-95: misma raza, color y rasgos visuales principales (muy probable que sea el mismo perro).
            - 70-84: similitud fuerte; diferencias menores (collar, chapita, ángulo de foto, patas no visibles).
            - Menos de 60: solo si claramente parecen perros distintos.
            - Collares y accesorios pueden cambiar entre reportes; no penalices fuerte solo por eso.

            Respondé SOLO JSON válido, sin markdown ni texto extra.
            En matches[].postId usá EXACTAMENTE el valor id= de cada candidato (UUID), sin inventar ids.
            {
              "aiAnalysis": "descripción visual + observaciones (mencionar si difiere de lo reportado)",
              "matches": [
                { "postId": "uuid_exacto_del_candidato", "matchPercentage": 87, "reason": "motivo breve" }
              ]
            }
        """.trimIndent()
    }

    companion object {
        private const val TAG = "GeminiMatchAnalyzer"
        private const val RETRY_DELAY_MS = 2_000L
        private const val MAX_ATTEMPTS_PER_MODEL = 2
        /** Modelos disponibles con keys AQ. en junio 2026 (1.5 ya no existe → 404). */
        val MODEL_FALLBACK_CHAIN = listOf(
            "gemini-2.5-flash-lite",
            "gemini-3.1-flash-lite",
            "gemini-3.5-flash",
            "gemini-2.5-flash",
            "gemini-flash-latest"
        )
    }
}
