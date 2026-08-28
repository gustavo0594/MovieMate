package com.gquesada.moviemate.domain.model

/** The user-context half of a recommendation request (design doc &sect;03/&sect;07). */
data class TasteSignals(
    val watchedWithRatings: List<MovieWithUserState>,
    val favoriteMovies: List<Movie>,
    val watchlistMovies: List<Movie>,
    /** Past recommendations the user never acted on (design doc &sect;27's feedback loop). */
    val declinedMovies: List<Movie> = emptyList(),
)
