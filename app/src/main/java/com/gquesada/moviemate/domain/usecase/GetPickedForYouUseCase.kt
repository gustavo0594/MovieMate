package com.gquesada.moviemate.domain.usecase

import com.gquesada.moviemate.domain.model.Movie
import com.gquesada.moviemate.domain.repository.RecommendationRepository

/**
 * Home's "Picked for You" row (design doc &sect;06): same candidate set and taste signals as
 * Surprise Me, but ranked with the deterministic heuristic only -- Home shouldn't spend an
 * on-device Prompt API call every time it loads.
 */
class GetPickedForYouUseCase(
    private val buildCandidateSet: BuildCandidateSetUseCase,
    private val recommendationRepository: RecommendationRepository,
) {
    suspend operator fun invoke(count: Int = DEFAULT_COUNT): List<Movie> {
        val (candidates, signals) = buildCandidateSet()
        if (candidates.isEmpty()) return emptyList()
        return recommendationRepository.pickForYou(candidates, signals, count)
    }

    private companion object {
        const val DEFAULT_COUNT = 10
    }
}
