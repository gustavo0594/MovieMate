package com.gquesada.moviemate.presentation.recommendation

import com.gquesada.moviemate.domain.model.Recommendation

data class SurpriseMeUiState(
    val isLoading: Boolean = true,
    val recommendation: Recommendation? = null,
    val error: String? = null,
)
