package com.gquesada.moviemate.data.ai

import com.gquesada.moviemate.domain.model.Movie
import com.gquesada.moviemate.domain.model.Recommendation
import com.gquesada.moviemate.domain.model.TasteSignals

/**
 * One strategy for turning candidates + signals into a pick.
 * Returns null when this strategy could not produce a trustworthy result --
 * the caller (see [com.gquesada.moviemate.data.repository.RecommendationRepositoryImpl])
 * is expected to fall back to a strategy that always succeeds.
 */
interface RecommendationEngine {
    suspend fun recommend(candidates: List<Movie>, signals: TasteSignals, userMood: String?): Recommendation?
}
