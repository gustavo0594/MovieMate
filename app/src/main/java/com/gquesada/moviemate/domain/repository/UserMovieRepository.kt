package com.gquesada.moviemate.domain.repository

import com.gquesada.moviemate.domain.model.MovieWithUserState
import com.gquesada.moviemate.domain.model.UserMovieState
import kotlinx.coroutines.flow.Flow

/** The entire "user data" boundary (design doc &sect;08) -- local-only, never synced. */
interface UserMovieRepository {
    fun observeUserState(movieId: Int): Flow<UserMovieState>
    fun observeFavorites(): Flow<List<MovieWithUserState>>
    fun observeWatched(): Flow<List<MovieWithUserState>>
    fun observeWatchlist(): Flow<List<MovieWithUserState>>

    suspend fun setFavorite(movieId: Int, isFavorite: Boolean)
    suspend fun setInWatchlist(movieId: Int, inWatchlist: Boolean)
    suspend fun setWatched(movieId: Int, watched: Boolean, personalRating: Int? = null)
}
