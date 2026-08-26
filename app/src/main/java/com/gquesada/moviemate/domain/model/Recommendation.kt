package com.gquesada.moviemate.domain.model

/** Result of "Surprise Me": one pick, why it was picked, and a couple of runners-up. */
data class Recommendation(
    val movie: Movie,
    val matchScore: Int,
    val reason: String,
    val alternates: List<Movie> = emptyList(),
    /** True when this came from the on-device model; false when the fallback ranker chose it. */
    val fromAi: Boolean,
)

enum class HomeSectionType { PICKED_FOR_YOU, POPULAR, TOP_RATED, NOW_PLAYING, UPCOMING }

data class HomeSection(
    val type: HomeSectionType,
    val movies: List<Movie>,
)
