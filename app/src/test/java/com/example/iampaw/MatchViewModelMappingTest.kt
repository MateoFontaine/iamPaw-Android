package com.example.iampaw

import com.example.iampaw.components.feed.DogPost
import com.example.iampaw.components.match.MatchCandidateMapper
import com.example.iampaw.data.ai.ScoredMatch
import org.junit.Assert.assertEquals
import org.junit.Test

class MatchViewModelMappingTest {

    private val candidates = listOf(
        DogPost("id-1", "Luna", "Border Collie", "CABA", "Hace 2h", "", "Encontrado"),
        DogPost("id-2", "Rocco", "Golden", "Palermo", "Ayer", "", "Encontrado")
    )

    @Test
    fun `mapea scored matches a MatchedDog ordenados por porcentaje`() {
        val scored = listOf(
            ScoredMatch(postId = "id-1", matchPercentage = 72, reason = "Pelaje similar"),
            ScoredMatch(postId = "id-2", matchPercentage = 91, reason = "Misma zona")
        )

        val result = MatchCandidateMapper.mapScoredMatches(scored, candidates)

        assertEquals(2, result.size)
        assertEquals("id-2", result[0].postId)
        assertEquals(91, result[0].matchPercentage)
        assertEquals("Rocco", result[0].name)
        assertEquals("Misma zona", result[0].reason)
    }

    @Test
    fun `fallback con un solo candidato aunque postId sea invalido`() {
        val scored = listOf(
            ScoredMatch(postId = "id-999", matchPercentage = 88, reason = "Muy similar")
        )

        val result = MatchCandidateMapper.mapScoredMatches(scored, listOf(candidates[0]))

        assertEquals(1, result.size)
        assertEquals("id-1", result[0].postId)
        assertEquals(88, result[0].matchPercentage)
    }

    @Test
    fun `resuelve candidato por nombre si Gemini manda el nombre`() {
        val scored = listOf(
            ScoredMatch(postId = "Luna", matchPercentage = 75, reason = "Misma raza")
        )

        val result = MatchCandidateMapper.mapScoredMatches(scored, candidates)

        assertEquals(1, result.size)
        assertEquals("id-1", result[0].postId)
    }
}
