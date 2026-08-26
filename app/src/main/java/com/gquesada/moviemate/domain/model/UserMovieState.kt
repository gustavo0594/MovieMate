package com.gquesada.moviemate.domain.model

/**
 * Everything this device knows about the viewer's relationship to one movie.
 * Favorite, watched and watchlist are independent flags -- never mutually exclusive.
 */
data class UserMovieState(
    val movieId: Int,
    val isFavorite: Boolean = false,
    val isWatched: Boolean = false,
    val isInWatchlist: Boolean = false,
    val personalRating: Int? = null,
    val watchedAt: Long? = null,
    val addedToWatchlistAt: Long? = null,
) {
    companion object {
        fun empty(movieId: Int) = UserMovieState(movieId = movieId)
    }
}
