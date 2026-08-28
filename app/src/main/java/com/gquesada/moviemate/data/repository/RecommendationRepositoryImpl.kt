package com.gquesada.moviemate.data.repository

import com.gquesada.moviemate.data.ai.FallbackHeuristicRanker
import com.gquesada.moviemate.data.ai.PromptRecommendationEngine
import com.gquesada.moviemate.data.ai.RecommendationScoring
import com.gquesada.moviemate.data.local.dao.RecommendationLogDao
import com.gquesada.moviemate.data.local.entity.RecommendationLogEntity
import com.gquesada.moviemate.domain.model.ModelAvailability
import com.gquesada.moviemate.domain.model.Movie
import com.gquesada.moviemate.domain.model.Recommendation
import com.gquesada.moviemate.domain.model.TasteSignals
import com.gquesada.moviemate.domain.repository.RecommendationRepository
import kotlinx.coroutines.flow.Flow

private const val ALTERNATE_COUNT = 2

/** How many of the most recently recommended movies "Surprise Me" avoids repeating. */
private const val RECENT_EXCLUDE_LIMIT = 5

/** How many past declined recommendations feed the negative genre-affinity signal. */
private const val DECLINED_SIGNAL_LIMIT = 20

class RecommendationRepositoryImpl(
    private val promptEngine: PromptRecommendationEngine,
    private val fallbackRanker: FallbackHeuristicRanker,
    private val recommendationLogDao: RecommendationLogDao,
) : RecommendationRepository {

    override suspend fun recommend(candidates: List<Movie>, signals: TasteSignals, userMood: String?): Recommendation {
        val recentIds = recommendationLogDao.getRecentlyRecommendedIds(RECENT_EXCLUDE_LIMIT).toSet()
        val pool = candidates.filterNot { it.tmdbId in recentIds }.ifEmpty { candidates }
        val enrichedSignals = signals.copy(declinedMovies = resolveDeclinedMovies(candidates))

        val picked = runCatching { promptEngine.recommend(pool, enrichedSignals, userMood) }.getOrNull()
            ?: fallbackRanker.recommend(pool, enrichedSignals, userMood)

        val alternates = pool
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

    override suspend fun markAccepted(movieId: Int) {
        recommendationLogDao.markAccepted(movieId)
    }

    override fun observeModelAvailability(): Flow<ModelAvailability> = promptEngine.modelAvailability

    override suspend fun pickForYou(candidates: List<Movie>, signals: TasteSignals, count: Int): List<Movie> {
        val watchedIds = signals.watchedWithRatings.map { it.movie.tmdbId }.toSet()
        val enrichedSignals = signals.copy(declinedMovies = resolveDeclinedMovies(candidates))
        val genreAffinity = RecommendationScoring.genreAffinity(enrichedSignals)
        return candidates
            .filterNot { it.tmdbId in watchedIds }
            .sortedByDescending { RecommendationScoring.affinityScore(it, genreAffinity) }
            .take(count)
    }

    override suspend fun tonightsPick(candidates: List<Movie>, signals: TasteSignals): Recommendation? {
        if (candidates.isEmpty()) return null
        val enrichedSignals = signals.copy(declinedMovies = resolveDeclinedMovies(candidates))
        return fallbackRanker.recommend(candidates, enrichedSignals, userMood = null)
    }

    /** Resolves declined-recommendation ids against [candidates] -- no separate DB join needed. */
    private suspend fun resolveDeclinedMovies(candidates: List<Movie>): List<Movie> {
        val declinedIds = recommendationLogDao.getDeclinedMovieIds(DECLINED_SIGNAL_LIMIT).toSet()
        if (declinedIds.isEmpty()) return emptyList()
        return candidates.filter { it.tmdbId in declinedIds }
    }
}
