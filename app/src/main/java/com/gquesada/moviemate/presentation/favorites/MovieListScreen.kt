package com.gquesada.moviemate.presentation.favorites

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.gquesada.moviemate.domain.model.MovieWithUserState
import com.gquesada.moviemate.presentation.components.PrimaryTopBar
import com.gquesada.moviemate.presentation.components.TmdbImage
import org.koin.androidx.compose.koinViewModel

@Composable
fun MovieListScreen(
    listType: MovieListType,
    onSearchClick: () -> Unit,
    onMovieClick: (Int) -> Unit,
) {
    val movies by when (listType) {
        MovieListType.FAVORITES -> koinViewModel<FavoritesViewModel>().movies.collectAsStateWithLifecycle()
        MovieListType.WATCHED -> koinViewModel<WatchedViewModel>().movies.collectAsStateWithLifecycle()
        MovieListType.WATCHLIST -> koinViewModel<WatchlistViewModel>().movies.collectAsStateWithLifecycle()
    }

    Scaffold(topBar = { PrimaryTopBar(title = listType.title, onSearchClick = onSearchClick) }) { padding ->
        if (movies.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center,
            ) { Text(listType.emptyMessage) }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(128.dp),
                contentPadding = PaddingValues(
                    start = 16.dp,
                    end = 16.dp,
                    top = padding.calculateTopPadding() + 16.dp,
                    bottom = padding.calculateBottomPadding() + 16.dp,
                ),
                modifier = Modifier.fillMaxSize(),
            ) {
                items(movies, key = { it.movie.tmdbId }) { entry ->
                    MovieGridItem(entry = entry, onClick = { onMovieClick(entry.movie.tmdbId) })
                }
            }
        }
    }
}

@Composable
private fun MovieGridItem(entry: MovieWithUserState, onClick: () -> Unit) {
    Column(modifier = Modifier.width(128.dp).clickable(onClick = onClick)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(2f / 3f)
                .clip(RoundedCornerShape(10.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant),
        ) {
            AsyncImage(
                model = TmdbImage.poster(entry.movie.posterPath),
                contentDescription = entry.movie.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.matchParentSize(),
            )
        }
        Text(
            text = entry.movie.title,
            style = MaterialTheme.typography.bodySmall,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
        entry.userState.personalRating?.let { rating ->
            Text(
                text = "★ $rating/5",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
            )
        } ?: run {
            if (entry.userState.isWatched) {
                Text("Watched", style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}
