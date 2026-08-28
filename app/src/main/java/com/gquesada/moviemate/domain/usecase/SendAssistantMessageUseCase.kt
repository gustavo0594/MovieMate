package com.gquesada.moviemate.domain.usecase

import com.gquesada.moviemate.domain.model.ChatMessage
import com.gquesada.moviemate.domain.repository.AssistantRepository

/** One turn of the AI Movie Assistant flow (design doc &sect;17): fresh candidates each turn, then ask. */
class SendAssistantMessageUseCase(
    private val buildCandidateSet: BuildCandidateSetUseCase,
    private val assistantRepository: AssistantRepository,
) {
    suspend operator fun invoke(history: List<ChatMessage>, userMessage: String): ChatMessage {
        val (candidates, signals) = buildCandidateSet()
        require(candidates.isNotEmpty()) {
            "No candidate movies yet -- browse Home or Search first so there's something to recommend from."
        }
        return assistantRepository.sendMessage(history, userMessage, candidates, signals)
    }
}
