package com.gquesada.moviemate.presentation.moviedetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gquesada.moviemate.domain.repository.MovieRepository
import com.gquesada.moviemate.domain.repository.UserMovieRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MovieDetailViewModel(
    private val movieId: Int,
    private val movieRepository: MovieRepository,
    private val userMovieRepository: UserMovieRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(MovieDetailUiState())
    val uiState: StateFlow<MovieDetailUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            userMovieRepository.observeUserState(movieId).collect { state ->
                _uiState.update { it.copy(userState = state) }
            }
        }
        load()
    }

    private fun load() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            runCatching {
                movieRepository.getMovieDetail(movieId) to movieRepository.getSimilarMovies(movieId)
            }.onSuccess { (movie, similar) ->
                _uiState.update { it.copy(isLoading = false, movie = movie, similarMovies = similar) }
            }.onFailure { e ->
                _uiState.update { it.copy(isLoading = false, error = e.message ?: "Couldn't load this movie") }
            }
        }
    }

    fun toggleFavorite() {
        val isFavorite = _uiState.value.userState?.isFavorite ?: false
        viewModelScope.launch { userMovieRepository.setFavorite(movieId, !isFavorite) }
    }

    fun toggleWatchlist() {
        val inWatchlist = _uiState.value.userState?.isInWatchlist ?: false
        viewModelScope.launch { userMovieRepository.setInWatchlist(movieId, !inWatchlist) }
    }

    fun markWatched(rating: Int? = null) {
        viewModelScope.launch { userMovieRepository.setWatched(movieId, watched = true, personalRating = rating) }
    }

    fun clearWatched() {
        viewModelScope.launch { userMovieRepository.setWatched(movieId, watched = false) }
    }
}
