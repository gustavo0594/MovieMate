package com.gquesada.moviemate.presentation.tasteprofile

import com.gquesada.moviemate.domain.model.TasteProfile

data class TasteProfileUiState(
    val isLoading: Boolean = true,
    val profile: TasteProfile? = null,
    val error: String? = null,
)
