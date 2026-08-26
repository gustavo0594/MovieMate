package com.gquesada.moviemate.domain.usecase

import com.gquesada.moviemate.domain.model.Recommendation
import com.gquesada.moviemate.domain.repository.RecommendationRepository

/** The "Surprise Me" flow end to end (design doc &sect;15): build candidates, ask, log. */
class GetSurpriseMeRecommendationUseCase(
    private val buildCandidateSet: BuildCandidateSetUseCase,
    private val recommendationRepository: RecommendationRepository,
) {
    suspend operator fun invoke(userMood: String? = null): Recommendation {
        val (candidates, signals) = buildCandidateSet()
        require(candidates.isNotEmpty()) {
            "No candidate movies yet -- browse Home or Search first so the catalog has something to recommend from."
        }
        val recommendation = recommendationRepository.recommend(candidates, signals, userMood)
        recommendationRepository.logRecommendation(recommendation)
        return recommendation
    }
}
