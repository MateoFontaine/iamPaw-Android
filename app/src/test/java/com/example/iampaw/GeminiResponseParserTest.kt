package com.example.iampaw

import com.example.iampaw.components.feed.DogPost
import com.example.iampaw.components.report.ReportDraft
import com.example.iampaw.data.ai.GeminiResponseParser
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GeminiResponseParserTest {

    @Test
    fun `parsea json limpio`() {
        val raw = """
            {
              "aiAnalysis": "Perro mediano de pelaje dorado.",
              "matches": [
                { "postId": "abc-123", "matchPercentage": 87, "reason": "Misma raza y zona" }
              ]
            }
        """.trimIndent()

        val result = GeminiResponseParser.parse(raw).getOrThrow()

        assertEquals("Perro mediano de pelaje dorado.", result.aiAnalysis)
        assertEquals(1, result.matches.size)
        assertEquals("abc-123", result.matches[0].postId)
        assertEquals(87, result.matches[0].matchPercentage)
    }

    @Test
    fun `parsea json dentro de bloque markdown`() {
        val raw = """
            ```json
            {
              "aiAnalysis": "Observación visual",
              "matches": []
            }
            ```
        """.trimIndent()

        val result = GeminiResponseParser.parse(raw).getOrThrow()

        assertEquals("Observación visual", result.aiAnalysis)
        assertTrue(result.matches.isEmpty())
    }

    @Test
    fun `falla con json invalido`() {
        val result = GeminiResponseParser.parse("esto no es json")

        assertTrue(result.isFailure)
    }
}

class GetMatchCandidatesTest {

    @Test
    fun `draft perdido devuelve solo encontrados`() = runTest {
        val posts = listOf(
            DogPost("1", "A", "Labrador", "CABA", "Hace 1h", "", "Perdido"),
            DogPost("2", "B", "Pug", "CABA", "Hace 2h", "", "Encontrado"),
            DogPost("3", "C", "Caniche", "Rosario", "Hace 3h", "", "Encontrado")
        )
        val repo = FakePawRepository(posts)
        val draft = ReportDraft(
            name = "Max",
            breed = "Labrador",
            location = "CABA",
            imageUrl = "",
            status = "Perdido",
            colorText = "Negro",
            sizeText = "Mediano",
            detailsText = "Collar rojo"
        )

        val candidates = repo.getMatchCandidates(draft)

        assertEquals(2, candidates.size)
        assertTrue(candidates.all { it.status.contains("Encontrado", ignoreCase = true) })
    }
}
