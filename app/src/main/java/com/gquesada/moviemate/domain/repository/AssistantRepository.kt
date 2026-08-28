package com.gquesada.moviemate.domain.repository

import com.gquesada.moviemate.domain.model.ChatMessage
import com.gquesada.moviemate.domain.model.ModelAvailability
import com.gquesada.moviemate.domain.model.Movie
import com.gquesada.moviemate.domain.model.TasteSignals
import kotlinx.coroutines.flow.Flow

/** Bridges to the conversational recommendation intelligence (design doc &sect;17). */
interface AssistantRepository {
    suspend fun sendMessage(
        history: List<ChatMessage>,
        newUserMessage: String,
        candidates: List<Movie>,
        signals: TasteSignals,
    ): ChatMessage

    /** Readiness of the on-device model, so the UI can explain a slow download or an unsupported device. */
    fun observeModelAvailability(): Flow<ModelAvailability>
}
