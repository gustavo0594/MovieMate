package com.gquesada.moviemate.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/** The only table that constitutes "the user's data" (design doc &sect;08). */
@Entity(tableName = "user_movie_state")
data class UserMovieStateEntity(
    @PrimaryKey val movieId: Int,
    val isFavorite: Boolean = false,
    val isWatched: Boolean = false,
    val isInWatchlist: Boolean = false,
    val personalRating: Int? = null,
    val watchedAt: Long? = null,
    val addedToWatchlistAt: Long? = null,
)
