package com.gquesada.moviemate.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.ui.graphics.vector.ImageVector

data class TopLevelDestination(
    val route: Any,
    val icon: ImageVector,
    val label: String,
)

val TOP_LEVEL_DESTINATIONS = listOf(
    TopLevelDestination(HomeRoute, Icons.Filled.Home, "Home"),
    TopLevelDestination(WatchlistRoute, Icons.Filled.Bookmark, "Watchlist"),
    TopLevelDestination(FavoritesRoute, Icons.Filled.Favorite, "Favorites"),
    TopLevelDestination(WatchedRoute, Icons.Filled.CheckCircle, "Watched"),
)
