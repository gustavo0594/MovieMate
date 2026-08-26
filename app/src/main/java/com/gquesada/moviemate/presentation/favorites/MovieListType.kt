package com.gquesada.moviemate.presentation.favorites

/** Favorites, Watched and Watchlist (design doc &sect;09-&sect;11) share one screen shape over different data. */
enum class MovieListType(val title: String, val emptyMessage: String) {
    FAVORITES("Favorites", "Movies you favorite will show up here."),
    WATCHED("Watched", "Movies you mark as watched will show up here."),
    WATCHLIST("Watchlist", "Movies you want to watch will show up here."),
}
