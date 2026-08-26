package com.gquesada.moviemate.data.mapper

import com.gquesada.moviemate.data.local.entity.MovieEntity
import com.gquesada.moviemate.data.local.entity.UserMovieStateEntity
import com.gquesada.moviemate.data.remote.dto.CreditsDto
import com.gquesada.moviemate.data.remote.dto.MovieDetailDto
import com.gquesada.moviemate.data.remote.dto.MovieDto
import com.gquesada.moviemate.domain.model.Movie
import com.gquesada.moviemate.domain.model.UserMovieState

private const val MAX_BILLED_CAST = 6

fun CreditsDto.castNames(): List<String> =
    cast.sortedBy { it.order }.take(MAX_BILLED_CAST).map { it.name }

fun CreditsDto.directorNames(): List<String> =
    crew.filter { it.job == "Director" }.map { it.name }

/** List-endpoint DTOs never carry detail fields; preserve whatever was already cached for them. */
fun MovieDto.toEntity(existing: MovieEntity?, cachedAt: Long): MovieEntity = MovieEntity(
    tmdbId = id,
    title = title,
    overview = overview,
    posterPath = posterPath,
    backdropPath = backdropPath,
    releaseDate = releaseDate,
    runtimeMinutes = existing?.runtimeMinutes,
    voteAverage = voteAverage,
    popularity = popularity,
    genres = existing?.genres ?: emptyList(),
    cast = existing?.cast ?: emptyList(),
    directors = existing?.directors ?: emptyList(),
    trailerKey = existing?.trailerKey,
    cachedAt = cachedAt,
)

fun MovieDetailDto.toEntity(
    cast: List<String>,
    directors: List<String>,
    trailerKey: String?,
    cachedAt: Long,
): MovieEntity = MovieEntity(
    tmdbId = id,
    title = title,
    overview = overview,
    posterPath = posterPath,
    backdropPath = backdropPath,
    releaseDate = releaseDate,
    runtimeMinutes = runtime,
    voteAverage = voteAverage,
    popularity = popularity,
    genres = genres.map { it.name },
    cast = cast,
    directors = directors,
    trailerKey = trailerKey,
    cachedAt = cachedAt,
)

fun MovieEntity.toDomain(): Movie = Movie(
    tmdbId = tmdbId,
    title = title,
    overview = overview,
    posterPath = posterPath,
    backdropPath = backdropPath,
    releaseDate = releaseDate,
    runtimeMinutes = runtimeMinutes,
    voteAverage = voteAverage,
    popularity = popularity,
    genres = genres,
    cast = cast,
    directors = directors,
    trailerKey = trailerKey,
)

fun UserMovieStateEntity.toDomain(): UserMovieState = UserMovieState(
    movieId = movieId,
    isFavorite = isFavorite,
    isWatched = isWatched,
    isInWatchlist = isInWatchlist,
    personalRating = personalRating,
    watchedAt = watchedAt,
    addedToWatchlistAt = addedToWatchlistAt,
)
