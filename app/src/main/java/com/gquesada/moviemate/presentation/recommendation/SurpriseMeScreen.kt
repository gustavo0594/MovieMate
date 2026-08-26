package com.gquesada.moviemate.presentation.recommendation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.gquesada.moviemate.domain.model.Recommendation
import com.gquesada.moviemate.presentation.components.MovieRow
import com.gquesada.moviemate.presentation.components.TmdbImage
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SurpriseMeScreen(
    onBack: () -> Unit,
    onMovieClick: (Int) -> Unit,
    viewModel: SurpriseMeViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var mood by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Surprise Me") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") }
                },
            )
        },
    ) { padding ->
        when {
            uiState.isLoading -> Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center,
            ) { CircularProgressIndicator() }

            uiState.error != null -> Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(uiState.error ?: "Something went wrong")
                    OutlinedButton(onClick = { viewModel.surpriseMe() }, modifier = Modifier.padding(top = 12.dp)) {
                        Text("Try again")
                    }
                }
            }

            uiState.recommendation != null -> Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
            ) {
                RecommendationCard(
                    recommendation = uiState.recommendation!!,
                    onClick = { onMovieClick(uiState.recommendation!!.movie.tmdbId) },
                )

                OutlinedTextField(
                    value = mood,
                    onValueChange = { mood = it },
                    label = { Text("Tonight's mood (optional)") },
                    placeholder = { Text("e.g. something funny tonight") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().padding(top = 20.dp),
                )
                OutlinedButton(
                    onClick = { viewModel.surpriseMe(mood.ifBlank { null }) },
                    modifier = Modifier.padding(top = 12.dp),
                ) { Text("Pick again") }

                if (uiState.recommendation!!.alternates.isNotEmpty()) {
                    Text(
                        "Or maybe...",
                        style = MaterialTheme.typography.titleSmall,
                        modifier = Modifier.padding(top = 24.dp, bottom = 8.dp),
                    )
                    MovieRow(
                        movies = uiState.recommendation!!.alternates,
                        onMovieClick = onMovieClick,
                        modifier = Modifier.padding(horizontal = 0.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun RecommendationCard(recommendation: Recommendation, onClick: () -> Unit) {
    Card(shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
        Column {
            AsyncImage(
                model = TmdbImage.backdrop(recommendation.movie.backdropPath),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxWidth().aspectRatio(16f / 9f),
            )
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Your pick", style = MaterialTheme.typography.labelLarge)
                Text(
                    recommendation.movie.title,
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(top = 4.dp),
                )
                Text(
                    "${recommendation.matchScore}% match" + if (!recommendation.fromAi) " · on-device pick" else "",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 4.dp, bottom = 12.dp),
                )
                Text("Why this movie?", style = MaterialTheme.typography.labelLarge)
                Text(recommendation.reason, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = 4.dp))
                OutlinedButton(onClick = onClick, modifier = Modifier.padding(top = 16.dp)) {
                    Text("See details")
                }
            }
        }
    }
}
