package com.gquesada.moviemate.domain.usecase

import com.gquesada.moviemate.domain.repository.RecommendationRepository

/**
 * Closes the feedback loop (design doc &sect;27): call when the user favorites, watchlists,
 * or marks a movie watched, so a recommendation that led to real engagement is recorded as
 * accepted instead of the log column sitting unused forever.
 */
class MarkRecommendationAcceptedUseCase(
    private val recommendationRepository: RecommendationRepository,
) {
    suspend operator fun invoke(movieId: Int) = recommendationRepository.markAccepted(movieId)
}
