package com.gquesada.moviemate.data.repository

import com.gquesada.moviemate.data.ai.FallbackHeuristicRanker
import com.gquesada.moviemate.data.ai.PromptRecommendationEngine
import com.gquesada.moviemate.data.local.dao.RecommendationLogDao
import com.gquesada.moviemate.data.local.entity.RecommendationLogEntity
import com.gquesada.moviemate.domain.model.Movie
import com.gquesada.moviemate.domain.model.Recommendation
import com.gquesada.moviemate.domain.model.TasteSignals
import com.gquesada.moviemate.domain.repository.RecommendationRepository

private const val ALTERNATE_COUNT = 2

class RecommendationRepositoryImpl(
    private val promptEngine: PromptRecommendationEngine,
    private val fallbackRanker: FallbackHeuristicRanker,
    private val recommendationLogDao: RecommendationLogDao,
) : RecommendationRepository {

    override suspend fun recommend(candidates: List<Movie>, signals: TasteSignals, userMood: String?): Recommendation {
        val picked = runCatching { promptEngine.recommend(candidates, signals, userMood) }.getOrNull()
            ?: fallbackRanker.recommend(candidates, signals, userMood)

        val alternates = candidates
            .filter { it.tmdbId != picked.movie.tmdbId }
            .sortedByDescending { it.voteAverage }
            .take(ALTERNATE_COUNT)

        return picked.copy(alternates = alternates)
    }

    override suspend fun logRecommendation(recommendation: Recommendation) {
        recommendationLogDao.insert(
            RecommendationLogEntity(
                movieId = recommendation.movie.tmdbId,
                reason = recommendation.reason,
                matchScore = recommendation.matchScore,
                generatedAt = System.currentTimeMillis(),
            ),
        )
    }
}
