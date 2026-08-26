package com.gquesada.moviemate.domain.repository

import com.gquesada.moviemate.domain.model.Movie
import com.gquesada.moviemate.domain.model.Recommendation
import com.gquesada.moviemate.domain.model.TasteSignals

/**
 * Bridges to the on-device recommendation intelligence (ML Kit Prompt API, with a
 * deterministic fallback -- design doc &sect;07) and records the outcome so the
 * feedback loop in &sect;01 has something to learn from.
 */
interface RecommendationRepository {
    suspend fun recommend(
        candidates: List<Movie>,
        signals: TasteSignals,
        userMood: String? = null,
    ): Recommendation

    suspend fun logRecommendation(recommendation: Recommendation)
}
