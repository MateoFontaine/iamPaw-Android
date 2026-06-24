package com.example.iampaw.data.ai

internal object GeminiErrorMapper {

    fun toUserMessage(throwable: Throwable): String {
        val raw = buildString {
            append(throwable.message.orEmpty())
            throwable.cause?.message?.let { append(' ').append(it) }
        }
        return when {
            raw.contains("503", ignoreCase = true) ||
                raw.contains("high demand", ignoreCase = true) ||
                raw.contains("UNAVAILABLE", ignoreCase = true) ->
                "Gemini saturado. Usamos modo respaldo local o tocá «Reintentar con IA»."

            raw.contains("429", ignoreCase = true) ||
                raw.contains("quota", ignoreCase = true) ||
                raw.contains("RESOURCE_EXHAUSTED", ignoreCase = true) ->
                "Cuota de Gemini agotada. Creá otra API key en AI Studio o esperá unos minutos."

            raw.contains("404", ignoreCase = true) ||
                raw.contains("not found", ignoreCase = true) ||
                raw.contains("NOT_FOUND", ignoreCase = true) ->
                "Ese modelo de Gemini ya no existe. La app prueba otros automáticamente."

            raw.contains("API key", ignoreCase = true) ||
                raw.contains("API_KEY", ignoreCase = true) ||
                raw.contains("API_KEY_INVALID", ignoreCase = true) ->
                "API key inválida. Revisá local.properties y hacé Rebuild."

            raw.contains("Unable to resolve host", ignoreCase = true) ||
                raw.contains("Network", ignoreCase = true) ||
                raw.contains("timeout", ignoreCase = true) ->
                "Sin conexión estable. Revisá internet."

            else -> "Gemini no respondió. Modo respaldo local activado si hay candidatos."
        }
    }

    fun isRetryable(throwable: Throwable): Boolean {
        val raw = throwable.message.orEmpty()
        return raw.contains("503") ||
            raw.contains("high demand", ignoreCase = true) ||
            raw.contains("UNAVAILABLE", ignoreCase = true) ||
            raw.contains("404") ||
            raw.contains("not found", ignoreCase = true) ||
            raw.contains("429") ||
            raw.contains("quota", ignoreCase = true) ||
            raw.contains("timeout", ignoreCase = true)
    }
}
