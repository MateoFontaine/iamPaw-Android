package com.example.iampaw.data.ai

import android.util.Log
import com.example.iampaw.BuildConfig
import com.example.iampaw.components.feed.DogPost
import com.example.iampaw.components.report.ReportDraft
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GeminiMatchAnalyzer @Inject constructor() {

    suspend fun analyzeReport(
        draft: ReportDraft,
        candidates: List<DogPost>,
        imageBytes: ByteArray?
    ): Result<GeminiMatchResult> {
        val apiKey = BuildConfig.GEMINI_API_KEY.trim()
        if (apiKey.isBlank()) {
            Log.w(TAG, "GEMINI_API_KEY vacía — agregala en local.properties y hacé Sync Gradle")
            return Result.failure(IllegalStateException("API key de Gemini no configurada"))
        }

        return try {
            val model = GenerativeModel(modelName = MODEL_NAME, apiKey = apiKey)
            val prompt = buildPrompt(draft, candidates)
            val bytes = imageBytes
            val input = content {
                if (bytes != null && bytes.isNotEmpty()) {
                    blob("image/jpeg", bytes)
                }
                text(prompt)
            }
            val response = model.generateContent(input)
            val rawText = response.text.orEmpty()
            Log.d(TAG, "Gemini respondió (${rawText.length} chars)")
            GeminiResponseParser.parse(rawText)
        } catch (e: Exception) {
            Log.e(TAG, "Gemini analyzeReport falló: ${e.message}", e)
            Result.failure(e)
        }
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

            Respondé SOLO JSON válido, sin markdown ni texto extra:
            {
              "aiAnalysis": "descripción visual + observaciones (mencionar si difiere de lo reportado)",
              "matches": [
                { "postId": "id_del_candidato", "matchPercentage": 87, "reason": "motivo breve" }
              ]
            }
        """.trimIndent()
    }

    companion object {
        private const val TAG = "GeminiMatchAnalyzer"
        private const val MODEL_NAME = "gemini-2.0-flash"
    }
}
