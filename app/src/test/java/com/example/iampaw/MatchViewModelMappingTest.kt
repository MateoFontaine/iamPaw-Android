package com.example.iampaw

import com.example.iampaw.components.feed.DogPost
import com.example.iampaw.components.match.MatchViewModel
import com.example.iampaw.components.match.MatchedDog
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

        val result = MatchViewModel.mapScoredMatches(scored, candidates)

        assertEquals(2, result.size)
        assertEquals("id-2", result[0].postId)
        assertEquals(91, result[0].matchPercentage)
        assertEquals("Rocco", result[0].name)
        assertEquals("Misma zona", result[0].reason)
    }

    @Test
    fun `ignora postId desconocidos`() {
        val scored = listOf(
            ScoredMatch(postId = "id-999", matchPercentage = 50, reason = "X")
        )

        val result = MatchViewModel.mapScoredMatches(scored, candidates)

        assertEquals(0, result.size)
    }
}
