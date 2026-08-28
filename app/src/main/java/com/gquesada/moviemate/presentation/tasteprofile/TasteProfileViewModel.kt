package com.gquesada.moviemate.presentation.tasteprofile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gquesada.moviemate.domain.usecase.GetTasteProfileUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class TasteProfileViewModel(
    private val getTasteProfile: GetTasteProfileUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(TasteProfileUiState())
    val uiState: StateFlow<TasteProfileUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    private fun load() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            runCatching { getTasteProfile() }
                .onSuccess { profile -> _uiState.update { it.copy(isLoading = false, profile = profile) } }
                .onFailure { e -> _uiState.update { it.copy(isLoading = false, error = e.message ?: "Couldn't load your taste profile") } }
        }
    }
}
