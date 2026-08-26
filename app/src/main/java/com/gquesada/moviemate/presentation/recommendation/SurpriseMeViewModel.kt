package com.gquesada.moviemate.presentation.recommendation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gquesada.moviemate.domain.usecase.GetSurpriseMeRecommendationUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SurpriseMeViewModel(
    private val getSurpriseMeRecommendation: GetSurpriseMeRecommendationUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SurpriseMeUiState())
    val uiState: StateFlow<SurpriseMeUiState> = _uiState.asStateFlow()

    init {
        surpriseMe()
    }

    fun surpriseMe(mood: String? = null) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            runCatching { getSurpriseMeRecommendation(mood) }
                .onSuccess { recommendation -> _uiState.update { it.copy(isLoading = false, recommendation = recommendation) } }
                .onFailure { e -> _uiState.update { it.copy(isLoading = false, error = e.message ?: "Couldn't get a recommendation") } }
        }
    }
}
