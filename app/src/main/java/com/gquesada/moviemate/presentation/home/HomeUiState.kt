package com.gquesada.moviemate.presentation.home

import com.gquesada.moviemate.domain.model.HomeSection
import com.gquesada.moviemate.domain.model.Recommendation

data class HomeUiState(
    val isLoading: Boolean = false,
    val tonightsPick: Recommendation? = null,
    val sections: List<HomeSection> = emptyList(),
    val error: String? = null,
)
