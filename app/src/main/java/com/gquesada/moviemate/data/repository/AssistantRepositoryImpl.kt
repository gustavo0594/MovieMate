package com.gquesada.moviemate.data.repository

import com.gquesada.moviemate.data.ai.ConversationalPromptEngine
import com.gquesada.moviemate.domain.model.ChatMessage
import com.gquesada.moviemate.domain.model.ChatRole
import com.gquesada.moviemate.domain.model.ModelAvailability
import com.gquesada.moviemate.domain.model.Movie
import com.gquesada.moviemate.domain.model.TasteSignals
import com.gquesada.moviemate.domain.repository.AssistantRepository
import kotlinx.coroutines.flow.Flow

class AssistantRepositoryImpl(
    private val conversationalEngine: ConversationalPromptEngine,
) : AssistantRepository {

    override suspend fun sendMessage(
        history: List<ChatMessage>,
        newUserMessage: String,
        candidates: List<Movie>,
        signals: TasteSignals,
    ): ChatMessage {
        // No fallback-ranker equivalent for chat replies -- if the model isn't ready, the UI
        // surfaces that via modelAvailability rather than getting a canned reply here.
        return conversationalEngine.sendMessage(history, newUserMessage, candidates, signals)
            ?: ChatMessage(
                role = ChatRole.ASSISTANT,
                text = "I couldn't come up with a pick just now -- want to try rephrasing that?",
            )
    }

    override fun observeModelAvailability(): Flow<ModelAvailability> = conversationalEngine.modelAvailability
}
