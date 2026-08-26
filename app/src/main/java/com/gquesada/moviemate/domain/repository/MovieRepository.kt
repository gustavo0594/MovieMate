package com.gquesada.moviemate.domain.repository

import com.gquesada.moviemate.domain.model.Movie

/**
 * TMDB-backed movie catalog. Every method also writes through to the local
 * cache so the movie is available offline and can become a recommendation
 * candidate later (design doc &sect;14).
 */
interface MovieRepository {
    suspend fun getPopularMovies(page: Int = 1): List<Movie>
    suspend fun getTopRatedMovies(page: Int = 1): List<Movie>
    suspend fun getNowPlayingMovies(page: Int = 1): List<Movie>
    suspend fun getUpcomingMovies(page: Int = 1): List<Movie>
    suspend fun searchMovies(query: String, page: Int = 1): List<Movie>
    suspend fun getMovieDetail(movieId: Int): Movie
    suspend fun getSimilarMovies(movieId: Int): List<Movie>

    /** Cached catalog only -- the pool candidate-building reads from, no network call. */
    suspend fun getCachedMovies(): List<Movie>
}
