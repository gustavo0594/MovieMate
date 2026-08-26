package com.gquesada.moviemate.presentation.components

import com.gquesada.moviemate.data.remote.TmdbApi

/** Resolves the raw poster/backdrop paths cached in [com.gquesada.moviemate.domain.model.Movie] into loadable URLs. */
object TmdbImage {
    private const val POSTER_SIZE = "w342"
    private const val BACKDROP_SIZE = "w780"

    fun poster(path: String?): String? = path?.let { "${TmdbApi.IMAGE_BASE_URL}$POSTER_SIZE$it" }
    fun backdrop(path: String?): String? = path?.let { "${TmdbApi.IMAGE_BASE_URL}$BACKDROP_SIZE$it" }
}
