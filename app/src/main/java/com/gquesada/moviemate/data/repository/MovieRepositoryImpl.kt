package com.gquesada.moviemate.data.repository

import com.gquesada.moviemate.data.local.dao.MovieDao
import com.gquesada.moviemate.data.mapper.castNames
import com.gquesada.moviemate.data.mapper.directorNames
import com.gquesada.moviemate.data.mapper.toDomain
import com.gquesada.moviemate.data.mapper.toEntity
import com.gquesada.moviemate.data.remote.TmdbApi
import com.gquesada.moviemate.data.remote.dto.MovieDto
import com.gquesada.moviemate.domain.model.Movie
import com.gquesada.moviemate.domain.repository.MovieRepository

class MovieRepositoryImpl(
    private val tmdbApi: TmdbApi,
    private val movieDao: MovieDao,
) : MovieRepository {

    override suspend fun getPopularMovies(page: Int): List<Movie> =
        cacheAndReturn(tmdbApi.getPopularMovies(page).results)

    override suspend fun getTopRatedMovies(page: Int): List<Movie> =
        cacheAndReturn(tmdbApi.getTopRatedMovies(page).results)

    override suspend fun getNowPlayingMovies(page: Int): List<Movie> =
        cacheAndReturn(tmdbApi.getNowPlayingMovies(page).results)

    override suspend fun getUpcomingMovies(page: Int): List<Movie> =
        cacheAndReturn(tmdbApi.getUpcomingMovies(page).results)

    override suspend fun searchMovies(query: String, page: Int): List<Movie> =
        cacheAndReturn(tmdbApi.searchMovies(query, page).results)

    override suspend fun getSimilarMovies(movieId: Int): List<Movie> =
        cacheAndReturn(tmdbApi.getSimilarMovies(movieId).results)

    override suspend fun getMovieDetail(movieId: Int): Movie {
        val detail = tmdbApi.getMovieDetail(movieId)
        val credits = tmdbApi.getCredits(movieId)
        val trailerKey = tmdbApi.getVideos(movieId).results
            .firstOrNull { it.site == "YouTube" && it.type == "Trailer" }
            ?.key

        val entity = detail.toEntity(
            cast = credits.castNames(),
            directors = credits.directorNames(),
            trailerKey = trailerKey,
            cachedAt = System.currentTimeMillis(),
        )
        movieDao.upsert(entity)
        return entity.toDomain()
    }

    override suspend fun getCachedMovies(): List<Movie> = movieDao.getCached().map { it.toDomain() }

    /** Caches list-endpoint results without clobbering any detail data already cached for them. */
    private suspend fun cacheAndReturn(dtos: List<MovieDto>): List<Movie> {
        val now = System.currentTimeMillis()
        val entities = dtos.map { dto -> dto.toEntity(existing = movieDao.getById(dto.id), cachedAt = now) }
        movieDao.upsertAll(entities)
        return entities.map { it.toDomain() }
    }
}
