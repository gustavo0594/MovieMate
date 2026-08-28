package com.gquesada.moviemate.presentation.assistant

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gquesada.moviemate.domain.model.ChatMessage
import com.gquesada.moviemate.domain.model.ChatRole
import com.gquesada.moviemate.domain.model.ModelAvailability
import com.gquesada.moviemate.presentation.components.ModelStatusBanner
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssistantScreen(
    onBack: () -> Unit,
    onMovieClick: (Int) -> Unit,
    viewModel: AssistantViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var input by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    LaunchedEffect(uiState.messages.size) {
        if (uiState.messages.isNotEmpty()) listState.animateScrollToItem(uiState.messages.lastIndex)
    }

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = { Text("Ask MovieMate") },
                    navigationIcon = {
                        IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") }
                    },
                )
                ModelStatusBanner(modelAvailability = uiState.modelAvailability)
            }
        },
        bottomBar = {
            AssistantInputBar(
                input = input,
                onInputChange = { input = it },
                isSending = uiState.isSending,
                error = uiState.error,
                modelAvailability = uiState.modelAvailability,
                onSend = {
                    val text = input
                    input = ""
                    viewModel.sendMessage(text)
                },
            )
        },
    ) { padding ->
        if (uiState.messages.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text(
                    "Ask about a mood, a runtime, or \"something like <movie>\" and MovieMate will pick from your local catalog.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(32.dp),
                )
            }
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(uiState.messages) { message -> MessageBubble(message, onMovieClick) }
            }
        }
    }
}

@Composable
private fun AssistantInputBar(
    input: String,
    onInputChange: (String) -> Unit,
    isSending: Boolean,
    error: String?,
    modelAvailability: ModelAvailability,
    onSend: () -> Unit,
) {
    Column {
        if (isSending) {
            Text(
                loadingLabel(modelAvailability),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
            )
        }
        if (error != null) {
            Text(
                error,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
            )
        }
        Row(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = input,
                onValueChange = onInputChange,
                placeholder = { Text("I want something like Interstellar, but shorter…") },
                modifier = Modifier.weight(1f),
                singleLine = true,
            )
            IconButton(onClick = onSend, enabled = input.isNotBlank() && !isSending) {
                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send")
            }
        }
    }
}

@Composable
private fun MessageBubble(message: ChatMessage, onMovieClick: (Int) -> Unit) {
    val isUser = message.role == ChatRole.USER
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start) {
        Column(modifier = Modifier.widthIn(max = 280.dp)) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isUser) {
                        MaterialTheme.colorScheme.primaryContainer
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant
                    },
                ),
            ) {
                Text(message.text, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(12.dp))
            }
            val movie = message.recommendedMovie
            if (movie != null) {
                Card(shape = RoundedCornerShape(12.dp), modifier = Modifier.padding(top = 8.dp).fillMaxWidth()) {
                    Column(modifier = Modifier.padding(12.dp).fillMaxWidth()) {
                        Text(movie.title, style = MaterialTheme.typography.titleSmall)
                        if (message.matchScore != null) {
                            Text(
                                "${message.matchScore}% match",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary,
                            )
                        }
                        OutlinedButton(onClick = { onMovieClick(movie.tmdbId) }, modifier = Modifier.padding(top = 8.dp)) {
                            Text("See details")
                        }
                    }
                }
            }
        }
    }
}

private fun loadingLabel(availability: ModelAvailability): String = when (availability) {
    is ModelAvailability.Downloading -> {
        val total = availability.totalBytes
        if (total != null && total > 0) {
            val percent = (availability.bytesDownloaded * 100 / total).coerceIn(0, 100)
            "Downloading on-device AI model… $percent%"
        } else {
            "Downloading on-device AI model…"
        }
    }
    ModelAvailability.Checking -> "Checking on-device AI availability…"
    ModelAvailability.Unsupported -> "This device doesn't support the on-device AI model."
    ModelAvailability.Error -> "On-device AI isn't available right now."
    ModelAvailability.Ready, ModelAvailability.Unknown -> "Thinking…"
}
