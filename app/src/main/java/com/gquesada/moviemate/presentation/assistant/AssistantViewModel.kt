package com.gquesada.moviemate.presentation.assistant

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gquesada.moviemate.domain.model.ChatMessage
import com.gquesada.moviemate.domain.model.ChatRole
import com.gquesada.moviemate.domain.usecase.ObserveAssistantModelAvailabilityUseCase
import com.gquesada.moviemate.domain.usecase.SendAssistantMessageUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AssistantViewModel(
    private val sendAssistantMessage: SendAssistantMessageUseCase,
    observeModelAvailability: ObserveAssistantModelAvailabilityUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AssistantUiState())
    val uiState: StateFlow<AssistantUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            observeModelAvailability().collect { availability ->
                _uiState.update { it.copy(modelAvailability = availability) }
            }
        }
    }

    fun sendMessage(text: String) {
        if (text.isBlank()) return
        val userMessage = ChatMessage(role = ChatRole.USER, text = text)
        val historyBeforeSend = _uiState.value.messages
        _uiState.update { it.copy(messages = it.messages + userMessage, isSending = true, error = null) }

        viewModelScope.launch {
            runCatching { sendAssistantMessage(historyBeforeSend, text) }
                .onSuccess { reply -> _uiState.update { it.copy(isSending = false, messages = it.messages + reply) } }
                .onFailure { e -> _uiState.update { it.copy(isSending = false, error = e.message ?: "Couldn't reach the assistant") } }
        }
    }
}
