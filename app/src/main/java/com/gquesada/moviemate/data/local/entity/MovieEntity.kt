package com.gquesada.moviemate.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/** Cached TMDB facts (design doc &sect;08) -- never user-specific, safe to evict/refetch. */
@Entity(tableName = "movies")
data class MovieEntity(
    @PrimaryKey val tmdbId: Int,
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
    val cachedAt: Long,
)
