package com.gquesada.moviemate.presentation.tasteprofile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gquesada.moviemate.domain.model.GenreShare
import com.gquesada.moviemate.domain.model.TasteProfile
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TasteProfileScreen(
    onBack: () -> Unit,
    viewModel: TasteProfileViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Your Taste") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") }
                },
            )
        },
    ) { padding ->
        val profile = uiState.profile
        when {
            uiState.isLoading -> Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            uiState.error != null -> Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text(uiState.error ?: "Something went wrong")
            }
            profile != null -> TasteProfileContent(profile, Modifier.padding(padding))
        }
    }
}

@Composable
private fun TasteProfileContent(profile: TasteProfile, modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxSize().padding(20.dp)) {
        if (profile.topGenres.isEmpty()) {
            Text(
                "Watch and rate a few movies, and your taste profile will show up here.",
                style = MaterialTheme.typography.bodyMedium,
            )
        } else {
            Text("Top genres", style = MaterialTheme.typography.titleMedium)
            Column(modifier = Modifier.padding(top = 12.dp, bottom = 24.dp)) {
                profile.topGenres.forEach { GenreBar(it) }
            }
            StatRow("Average personal rating", profile.averagePersonalRating?.let { "★ %.1f".format(it) } ?: "—")
            StatRow("Movies watched", profile.watchedCount.toString())
            StatRow("Favorites", profile.favoritesCount.toString())
        }
    }
}

@Composable
private fun GenreBar(genreShare: GenreShare) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
        Text(genreShare.genre, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.width(96.dp))
        val fraction = genreShare.share.toFloat().coerceIn(0.05f, 1f)
        Box(
            modifier = Modifier
                .weight(fraction)
                .height(16.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.primary),
        )
        if (fraction < 1f) {
            Box(modifier = Modifier.weight(1f - fraction))
        }
    }
}

@Composable
private fun StatRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium)
        Text(value, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
    }
}
