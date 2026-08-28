package com.gquesada.moviemate.domain.usecase

import com.gquesada.moviemate.domain.model.ModelAvailability
import com.gquesada.moviemate.domain.repository.RecommendationRepository
import kotlinx.coroutines.flow.Flow

/** Lets the UI explain a slow first-run model download, or that this device can't run it at all. */
class ObserveModelAvailabilityUseCase(
    private val recommendationRepository: RecommendationRepository,
) {
    operator fun invoke(): Flow<ModelAvailability> = recommendationRepository.observeModelAvailability()
}
