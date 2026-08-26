package com.gquesada.moviemate.presentation.moviedetail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.AssistChip
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconToggleButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.gquesada.moviemate.domain.model.Movie
import com.gquesada.moviemate.domain.model.UserMovieState
import com.gquesada.moviemate.presentation.components.MovieRow
import com.gquesada.moviemate.presentation.components.StarRatingBar
import com.gquesada.moviemate.presentation.components.TmdbImage
import com.gquesada.moviemate.presentation.components.openTrailer
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MovieDetailScreen(
    movieId: Int,
    onBack: () -> Unit,
    onMovieClick: (Int) -> Unit,
    viewModel: MovieDetailViewModel = koinViewModel { parametersOf(movieId) },
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") }
                },
            )
        },
    ) { padding ->
        val movie = uiState.movie
        when {
            uiState.isLoading && movie == null -> Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center,
            ) { CircularProgressIndicator() }

            movie == null -> Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center,
            ) { Text(uiState.error ?: "Couldn't load this movie") }

            else -> LazyColumn(modifier = Modifier.fillMaxSize().padding(top = padding.calculateTopPadding())) {
                item {
                    AsyncImage(
                        model = TmdbImage.backdrop(movie.backdropPath),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxWidth().aspectRatio(16f / 9f),
                    )
                }
                item {
                    MovieHeader(movie)
                }
                item {
                    ActionRow(
                        userState = uiState.userState,
                        hasTrailer = movie.trailerKey != null,
                        onFavoriteClick = viewModel::toggleFavorite,
                        onWatchlistClick = viewModel::toggleWatchlist,
                        onWatchedClick = { if (uiState.userState?.isWatched == true) viewModel.clearWatched() else viewModel.markWatched() },
                        onTrailerClick = { movie.trailerKey?.let { openTrailer(context, it) } },
                    )
                }
                if (uiState.userState?.isWatched == true) {
                    item {
                        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                            Text("Your rating", style = MaterialTheme.typography.labelLarge)
                            StarRatingBar(
                                rating = uiState.userState?.personalRating ?: 0,
                                onRatingSelected = { viewModel.markWatched(it) },
                                modifier = Modifier.padding(top = 4.dp),
                            )
                        }
                    }
                }
                item {
                    Text(
                        text = movie.overview,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    )
                }
                if (movie.genres.isNotEmpty()) {
                    item {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                        ) {
                            movie.genres.forEach { genre -> AssistChip(onClick = {}, label = { Text(genre) }) }
                        }
                    }
                }
                if (movie.cast.isNotEmpty()) {
                    item {
                        Text(
                            "Cast: ${movie.cast.joinToString(", ")}",
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                        )
                    }
                }
                if (movie.directors.isNotEmpty()) {
                    item {
                        Text(
                            "Director: ${movie.directors.joinToString(", ")}",
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                        )
                    }
                }
                if (uiState.similarMovies.isNotEmpty()) {
                    item {
                        Text(
                            "Similar movies",
                            style = MaterialTheme.typography.titleSmall,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                        )
                    }
                    item {
                        MovieRow(movies = uiState.similarMovies, onMovieClick = onMovieClick)
                    }
                }
            }
        }
    }
}

@Composable
private fun MovieHeader(movie: Movie) {
    Row(modifier = Modifier.padding(16.dp)) {
        AsyncImage(
            model = TmdbImage.poster(movie.posterPath),
            contentDescription = movie.title,
            contentScale = ContentScale.Crop,
            modifier = Modifier.width(96.dp).aspectRatio(2f / 3f),
        )
        Column(modifier = Modifier.padding(start = 16.dp)) {
            Text(movie.title, style = MaterialTheme.typography.titleLarge)
            val year = movie.releaseDate.take(4)
            val runtime = movie.runtimeMinutes?.let { "${it} min" }
            Text(
                listOfNotNull(year.ifBlank { null }, runtime).joinToString(" · "),
                style = MaterialTheme.typography.bodyMedium,
            )
            Text("TMDB ${movie.voteAverage}/10", style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun ActionRow(
    userState: UserMovieState?,
    hasTrailer: Boolean,
    onFavoriteClick: () -> Unit,
    onWatchlistClick: () -> Unit,
    onWatchedClick: () -> Unit,
    onTrailerClick: () -> Unit,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.padding(horizontal = 16.dp),
    ) {
        FilledTonalIconToggleButton(checked = userState?.isFavorite == true, onCheckedChange = { onFavoriteClick() }) {
            Icon(
                if (userState?.isFavorite == true) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                contentDescription = "Favorite",
            )
        }
        FilledTonalIconToggleButton(checked = userState?.isInWatchlist == true, onCheckedChange = { onWatchlistClick() }) {
            Icon(
                if (userState?.isInWatchlist == true) Icons.Filled.Bookmark else Icons.Filled.BookmarkBorder,
                contentDescription = "Watchlist",
            )
        }
        FilledTonalIconToggleButton(checked = userState?.isWatched == true, onCheckedChange = { onWatchedClick() }) {
            Icon(Icons.Filled.CheckCircle, contentDescription = "Mark watched")
        }
        if (hasTrailer) {
            TextButton(onClick = onTrailerClick) {
                Icon(Icons.Filled.PlayArrow, contentDescription = null)
                Text("Trailer")
            }
        }
    }
}
