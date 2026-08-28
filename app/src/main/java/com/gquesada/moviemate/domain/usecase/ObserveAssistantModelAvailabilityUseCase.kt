package com.gquesada.moviemate.domain.usecase

import com.gquesada.moviemate.domain.model.ModelAvailability
import com.gquesada.moviemate.domain.repository.AssistantRepository
import kotlinx.coroutines.flow.Flow

/** Lets the Assistant UI explain a slow first-run model download, or an unsupported device. */
class ObserveAssistantModelAvailabilityUseCase(
    private val assistantRepository: AssistantRepository,
) {
    operator fun invoke(): Flow<ModelAvailability> = assistantRepository.observeModelAvailability()
}
