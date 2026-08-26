package com.gquesada.moviemate.presentation.moviedetail

import com.gquesada.moviemate.domain.model.Movie
import com.gquesada.moviemate.domain.model.UserMovieState

data class MovieDetailUiState(
    val isLoading: Boolean = true,
    val movie: Movie? = null,
    val userState: UserMovieState? = null,
    val similarMovies: List<Movie> = emptyList(),
    val error: String? = null,
)
