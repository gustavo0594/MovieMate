package com.gquesada.moviemate.presentation.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gquesada.moviemate.domain.model.MovieWithUserState
import com.gquesada.moviemate.domain.repository.UserMovieRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

private const val STOP_TIMEOUT_MS = 5_000L

abstract class BaseMovieListViewModel(moviesFlow: Flow<List<MovieWithUserState>>) : ViewModel() {
    val movies: StateFlow<List<MovieWithUserState>> =
        moviesFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), emptyList())
}

class FavoritesViewModel(repository: UserMovieRepository) : BaseMovieListViewModel(repository.observeFavorites())

class WatchedViewModel(repository: UserMovieRepository) : BaseMovieListViewModel(repository.observeWatched())

class WatchlistViewModel(repository: UserMovieRepository) : BaseMovieListViewModel(repository.observeWatchlist())
