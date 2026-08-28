package com.gquesada.moviemate.presentation.assistant

import com.gquesada.moviemate.domain.model.ChatMessage
import com.gquesada.moviemate.domain.model.ModelAvailability

data class AssistantUiState(
    val messages: List<ChatMessage> = emptyList(),
    val isSending: Boolean = false,
    val error: String? = null,
    val modelAvailability: ModelAvailability = ModelAvailability.Unknown,
)
