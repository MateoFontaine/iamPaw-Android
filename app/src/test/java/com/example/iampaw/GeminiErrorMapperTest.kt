package com.example.iampaw

import com.example.iampaw.data.ai.GeminiErrorMapper
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GeminiErrorMapperTest {

    @Test
    fun `mapea error 503 a mensaje amigable`() {
        val error = Exception(
            """Unexpected Response: { "error": { "code": 503, "message": "high demand", "status": "UNAVAILABLE" } }"""
        )

        val message = GeminiErrorMapper.toUserMessage(error)

        assertTrue(message.contains("saturado"))
        assertTrue(GeminiErrorMapper.isRetryable(error))
    }

    @Test
    fun `mapea api key invalida`() {
        val error = Exception("API_KEY_INVALID")

        val message = GeminiErrorMapper.toUserMessage(error)

        assertTrue(message.contains("API key"))
    }
}
