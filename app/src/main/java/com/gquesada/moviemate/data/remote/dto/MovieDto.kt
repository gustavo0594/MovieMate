package com.gquesada.moviemate.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Shape returned by list endpoints (popular/top_rated/now_playing/upcoming/search). */
@Serializable
data class MovieDto(
    val id: Int,
    val title: String,
    val overview: String,
    @SerialName("poster_path") val posterPath: String? = null,
    @SerialName("backdrop_path") val backdropPath: String? = null,
    @SerialName("release_date") val releaseDate: String = "",
    @SerialName("vote_average") val voteAverage: Double = 0.0,
    val popularity: Double = 0.0,
)

@Serializable
data class PagedResponseDto<T>(
    val page: Int,
    val results: List<T>,
    @SerialName("total_pages") val totalPages: Int = 1,
    @SerialName("total_results") val totalResults: Int = 0,
)

@Serializable
data class GenreDto(
    val id: Int,
    val name: String,
)

/** Shape returned by GET movie/{id} -- adds fields not present on list results. */
@Serializable
data class MovieDetailDto(
    val id: Int,
    val title: String,
    val overview: String,
    @SerialName("poster_path") val posterPath: String? = null,
    @SerialName("backdrop_path") val backdropPath: String? = null,
    @SerialName("release_date") val releaseDate: String = "",
    @SerialName("vote_average") val voteAverage: Double = 0.0,
    val popularity: Double = 0.0,
    val runtime: Int? = null,
    val genres: List<GenreDto> = emptyList(),
)
