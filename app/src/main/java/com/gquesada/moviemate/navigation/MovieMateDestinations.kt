package com.gquesada.moviemate.navigation

import kotlinx.serialization.Serializable

// Primary destinations (design doc &sect;04) -- always exactly these four, on every device size.
@Serializable object HomeRoute
@Serializable object WatchlistRoute
@Serializable object FavoritesRoute
@Serializable object WatchedRoute

// Secondary destinations -- pushed on top of whichever primary tab is active.
@Serializable object SearchRoute
@Serializable object SurpriseMeRoute
@Serializable data class MovieDetailRoute(val movieId: Int)
