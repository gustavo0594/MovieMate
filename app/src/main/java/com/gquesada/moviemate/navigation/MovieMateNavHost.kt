package com.gquesada.moviemate.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.gquesada.moviemate.presentation.favorites.MovieListScreen
import com.gquesada.moviemate.presentation.favorites.MovieListType
import com.gquesada.moviemate.presentation.home.HomeScreen
import com.gquesada.moviemate.presentation.moviedetail.MovieDetailScreen
import com.gquesada.moviemate.presentation.recommendation.SurpriseMeScreen
import com.gquesada.moviemate.presentation.search.SearchScreen

/** One nav graph, shared by every window size class (design doc &sect;05). */
@Composable
fun MovieMateNavHost(navController: NavHostController, modifier: Modifier = Modifier) {
    NavHost(navController = navController, startDestination = HomeRoute, modifier = modifier) {
        composable<HomeRoute> {
            HomeScreen(
                onSearchClick = { navController.navigate(SearchRoute) },
                onSurpriseMeClick = { navController.navigate(SurpriseMeRoute) },
                onMovieClick = { movieId -> navController.navigate(MovieDetailRoute(movieId)) },
            )
        }
        composable<WatchlistRoute> {
            MovieListScreen(
                listType = MovieListType.WATCHLIST,
                onSearchClick = { navController.navigate(SearchRoute) },
                onMovieClick = { movieId -> navController.navigate(MovieDetailRoute(movieId)) },
            )
        }
        composable<FavoritesRoute> {
            MovieListScreen(
                listType = MovieListType.FAVORITES,
                onSearchClick = { navController.navigate(SearchRoute) },
                onMovieClick = { movieId -> navController.navigate(MovieDetailRoute(movieId)) },
            )
        }
        composable<WatchedRoute> {
            MovieListScreen(
                listType = MovieListType.WATCHED,
                onSearchClick = { navController.navigate(SearchRoute) },
                onMovieClick = { movieId -> navController.navigate(MovieDetailRoute(movieId)) },
            )
        }
        composable<SearchRoute> {
            SearchScreen(
                onBack = { navController.popBackStack() },
                onMovieClick = { movieId -> navController.navigate(MovieDetailRoute(movieId)) },
            )
        }
        composable<MovieDetailRoute> { backStackEntry ->
            val route: MovieDetailRoute = backStackEntry.toRoute()
            MovieDetailScreen(
                movieId = route.movieId,
                onBack = { navController.popBackStack() },
                onMovieClick = { movieId -> navController.navigate(MovieDetailRoute(movieId)) },
            )
        }
        composable<SurpriseMeRoute> {
            SurpriseMeScreen(
                onBack = { navController.popBackStack() },
                onMovieClick = { movieId -> navController.navigate(MovieDetailRoute(movieId)) },
            )
        }
    }
}
