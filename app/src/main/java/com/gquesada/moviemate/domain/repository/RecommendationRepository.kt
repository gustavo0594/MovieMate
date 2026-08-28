package com.gquesada.moviemate.domain.repository

import com.gquesada.moviemate.domain.model.ModelAvailability
import com.gquesada.moviemate.domain.model.Movie
import com.gquesada.moviemate.domain.model.Recommendation
import com.gquesada.moviemate.domain.model.TasteSignals
import kotlinx.coroutines.flow.Flow

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

    /** Closes the feedback loop: called when the user favorites, watches, or watchlists a movie. */
    suspend fun markAccepted(movieId: Int)

    /** Readiness of the on-device model, so the UI can explain a slow download or an unsupported device. */
    fun observeModelAvailability(): Flow<ModelAvailability>

    /** Ranks candidates with the deterministic heuristic only -- no Prompt API call -- for Home's "Picked for You". */
    suspend fun pickForYou(candidates: List<Movie>, signals: TasteSignals, count: Int): List<Movie>

    /**
     * Home's live top pick (design doc &sect;06): same heuristic ranker as [pickForYou], but
     * shaped as a full [Recommendation] (with match score and reason) instead of a bare list,
     * so Home can show it the same way "Surprise Me" does. Never calls the Prompt API.
     */
    suspend fun tonightsPick(candidates: List<Movie>, signals: TasteSignals): Recommendation?
}
