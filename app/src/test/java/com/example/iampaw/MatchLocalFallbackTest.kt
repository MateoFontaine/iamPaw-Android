package com.example.iampaw

import com.example.iampaw.components.feed.DogPost
import com.example.iampaw.components.report.ReportDraft
import com.example.iampaw.data.ai.MatchLocalFallback
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MatchLocalFallbackTest {

    @Test
    fun `genera matches para candidatos opuestos`() {
        val draft = ReportDraft(
            name = "Rocco",
            breed = "Golden Retriever",
            location = "Pinamar, Mar del Plata",
            imageUrl = "",
            status = "Encontrado",
            colorText = "Dorado",
            sizeText = "Grande",
            detailsText = "Collar rojo"
        )
        val candidates = listOf(
            DogPost(
                id = "lost-1",
                name = "Rocco",
                breed = "Golden Retriever",
                location = "Pinamar",
                time = "Hace 2h",
                imageUrl = "",
                status = "Perdido",
                color = "Dorado"
            )
        )

        val result = MatchLocalFallback.analyze(draft, candidates)

        assertTrue(result.aiAnalysis.contains("respaldo") || result.aiAnalysis.contains("local"))
        assertEquals(1, result.matches.size)
        assertEquals("lost-1", result.matches[0].postId)
        assertTrue(result.matches[0].matchPercentage >= 50)
    }

    @Test
    fun `offline usa mensaje tecnico sin mencionar gemini`() {
        val draft = ReportDraft(
            name = "Luna",
            breed = "Mestizo",
            location = "Pinamar",
            imageUrl = "",
            status = "Encontrado",
            colorText = "Negro",
            sizeText = "Mediano",
            detailsText = ""
        )
        val candidates = listOf(
            DogPost("id-1", "Pandora", "Mestizo", "Pinamar", "Recién", "", "Perdido")
        )

        val result = MatchLocalFallback.analyze(
            draft,
            candidates,
            MatchLocalFallback.Reason.OFFLINE
        )

        assertTrue(result.aiAnalysis.contains("técnico local"))
        assertTrue(result.aiAnalysis.contains("sin WiFi"))
    }
}
