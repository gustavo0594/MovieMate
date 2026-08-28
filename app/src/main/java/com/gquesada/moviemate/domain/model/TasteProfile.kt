package com.gquesada.moviemate.domain.model

/** Movie Taste Profile (design doc &sect;18): derived from local watch history, never stored as static preferences. */
data class TasteProfile(
    val topGenres: List<GenreShare>,
    val averagePersonalRating: Double?,
    val watchedCount: Int,
    val favoritesCount: Int,
)

/** [share] is this genre's fraction of the user's total weighted genre signal, in [0, 1]. */
data class GenreShare(val genre: String, val share: Double)
