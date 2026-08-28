package com.gquesada.moviemate.presentation.moviedetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gquesada.moviemate.domain.repository.MovieRepository
import com.gquesada.moviemate.domain.repository.UserMovieRepository
import com.gquesada.moviemate.domain.usecase.MarkRecommendationAcceptedUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MovieDetailViewModel(
    private val movieId: Int,
    private val movieRepository: MovieRepository,
    private val userMovieRepository: UserMovieRepository,
    private val markRecommendationAccepted: MarkRecommendationAcceptedUseCase,
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
        val turningOn = !isFavorite
        viewModelScope.launch {
            userMovieRepository.setFavorite(movieId, turningOn)
            // Favoriting/watchlisting/watching a movie is a real signal that a past
            // recommendation for it landed -- close the loop design doc &sect;27 describes.
            if (turningOn) markRecommendationAccepted(movieId)
        }
    }

    fun toggleWatchlist() {
        val inWatchlist = _uiState.value.userState?.isInWatchlist ?: false
        val turningOn = !inWatchlist
        viewModelScope.launch {
            userMovieRepository.setInWatchlist(movieId, turningOn)
            if (turningOn) markRecommendationAccepted(movieId)
        }
    }

    fun markWatched(rating: Int? = null) {
        viewModelScope.launch {
            userMovieRepository.setWatched(movieId, watched = true, personalRating = rating)
            markRecommendationAccepted(movieId)
        }
    }

    fun clearWatched() {
        viewModelScope.launch { userMovieRepository.setWatched(movieId, watched = false) }
    }
}
