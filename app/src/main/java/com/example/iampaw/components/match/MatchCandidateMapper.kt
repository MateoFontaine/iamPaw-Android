package com.example.iampaw.components.match

import com.example.iampaw.components.feed.DogPost
import com.example.iampaw.data.ai.ScoredMatch

internal object MatchCandidateMapper {

    fun mapScoredMatches(
        scored: List<ScoredMatch>,
        candidates: List<DogPost>
    ): List<MatchedDog> {
        if (candidates.isEmpty() || scored.isEmpty()) return emptyList()

        val byId = candidates.associateBy { it.id }
        val mapped = scored.mapNotNull { match ->
            resolveCandidate(match.postId, candidates, byId)?.let { post ->
                toMatchedDog(post, match.matchPercentage, match.reason)
            }
        }
        if (mapped.isNotEmpty()) {
            return mapped.sortedByDescending { it.matchPercentage }
        }

        // Un solo candidato: usar el mejor score aunque Gemini haya errado el postId
        if (candidates.size == 1) {
            val best = scored.maxByOrNull { it.matchPercentage } ?: return emptyList()
            return listOf(toMatchedDog(candidates.first(), best.matchPercentage, best.reason))
        }

        return emptyList()
    }

    private fun resolveCandidate(
        rawPostId: String,
        candidates: List<DogPost>,
        byId: Map<String, DogPost>
    ): DogPost? {
        val postId = rawPostId.trim()
        if (postId.isEmpty()) return null

        byId[postId]?.let { return it }

        candidates.find { candidate ->
            candidate.id.equals(postId, ignoreCase = true) ||
                candidate.id.contains(postId, ignoreCase = true) ||
                postId.contains(candidate.id, ignoreCase = true)
        }?.let { return it }

        candidates.find { candidate ->
            candidate.name.equals(postId, ignoreCase = true) ||
                postId.contains(candidate.name, ignoreCase = true) ||
                candidate.name.contains(postId, ignoreCase = true)
        }?.let { return it }

        return null
    }

    private fun toMatchedDog(post: DogPost, percentage: Int, reason: String) = MatchedDog(
        postId = post.id,
        name = post.name,
        breed = post.breed,
        location = post.location,
        timeText = post.time,
        matchPercentage = percentage.coerceIn(0, 100),
        imageUrl = post.imageUrl,
        reason = reason
    )
}
