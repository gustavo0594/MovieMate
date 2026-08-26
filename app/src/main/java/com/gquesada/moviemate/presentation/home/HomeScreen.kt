package com.gquesada.moviemate.presentation.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.androidx.compose.koinViewModel
import com.gquesada.moviemate.domain.model.HomeSection
import com.gquesada.moviemate.domain.model.HomeSectionType
import com.gquesada.moviemate.presentation.components.MovieRow
import com.gquesada.moviemate.presentation.components.PrimaryTopBar

@Composable
fun HomeScreen(
    onSearchClick: () -> Unit,
    onSurpriseMeClick: () -> Unit,
    onMovieClick: (Int) -> Unit,
    viewModel: HomeViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(topBar = { PrimaryTopBar(title = "MovieMate", onSearchClick = onSearchClick) }) { padding ->
        when {
            uiState.isLoading && uiState.sections.isEmpty() -> Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center,
            ) { CircularProgressIndicator() }

            uiState.error != null && uiState.sections.isEmpty() -> Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center,
            ) { Text(uiState.error ?: "Couldn't load movies") }

            else -> LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(top = padding.calculateTopPadding(), bottom = 24.dp),
            ) {
                item { SurpriseMeHero(onClick = onSurpriseMeClick) }
                items(uiState.sections.filter { it.movies.isNotEmpty() }, key = { it.type }) { section ->
                    HomeSectionRow(section, onMovieClick)
                }
            }
        }
    }
}

@Composable
private fun SurpriseMeHero(onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(16.dp),
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text("What should I watch tonight?", style = MaterialTheme.typography.titleMedium)
            Text(
                "Picked from your own watch history -- not a random TMDB title.",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 4.dp, bottom = 16.dp),
            )
            Button(onClick = onClick) { Text("Surprise Me") }
        }
    }
}

@Composable
private fun HomeSectionRow(section: HomeSection, onMovieClick: (Int) -> Unit) {
    Column(modifier = Modifier.padding(bottom = 20.dp)) {
        Text(
            text = section.type.displayName(),
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        )
        MovieRow(movies = section.movies, onMovieClick = onMovieClick)
    }
}

private fun HomeSectionType.displayName(): String = when (this) {
    HomeSectionType.PICKED_FOR_YOU -> "Picked for You"
    HomeSectionType.POPULAR -> "Popular Movies"
    HomeSectionType.TOP_RATED -> "Top Rated"
    HomeSectionType.NOW_PLAYING -> "Now Playing"
    HomeSectionType.UPCOMING -> "Upcoming"
}
