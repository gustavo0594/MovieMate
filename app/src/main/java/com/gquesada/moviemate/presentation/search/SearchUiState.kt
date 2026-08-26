package com.gquesada.moviemate.presentation.search

import com.gquesada.moviemate.domain.model.Movie

data class SearchUiState(
    val query: String = "",
    val results: List<Movie> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
)
