package com.gquesada.moviemate.presentation.home

import com.gquesada.moviemate.domain.model.HomeSection

data class HomeUiState(
    val isLoading: Boolean = false,
    val sections: List<HomeSection> = emptyList(),
    val error: String? = null,
)
