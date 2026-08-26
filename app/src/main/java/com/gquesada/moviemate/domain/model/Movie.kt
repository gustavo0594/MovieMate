package com.gquesada.moviemate.domain.model

/**
 * Catalog facts about a movie, as known from TMDB. Contains nothing about
 * this user's relationship to the movie -- see [UserMovieState] for that.
 */
data class Movie(
    val tmdbId: Int,
    val title: String,
    val overview: String,
    val posterPath: String?,
    val backdropPath: String?,
    val releaseDate: String,
    val runtimeMinutes: Int?,
    val voteAverage: Double,
    val popularity: Double,
    val genres: List<String>,
    val cast: List<String>,
    val directors: List<String>,
    val trailerKey: String?,
)

/** [Movie] paired with the local, user-specific state saved for it, if any. */
data class MovieWithUserState(
    val movie: Movie,
    val userState: UserMovieState,
)
