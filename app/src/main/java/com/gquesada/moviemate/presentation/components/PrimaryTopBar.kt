package com.gquesada.moviemate.presentation.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable

/** Shared top bar for the four primary destinations -- Search lives here, not as a nav item (&sect;04). */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrimaryTopBar(
    title: String,
    onSearchClick: () -> Unit,
    onTasteProfileClick: (() -> Unit)? = null,
    onAssistantClick: (() -> Unit)? = null,
) {
    TopAppBar(
        title = { Text(title) },
        actions = {
            if (onAssistantClick != null) {
                IconButton(onClick = onAssistantClick) {
                    Icon(Icons.Filled.Chat, contentDescription = "Ask MovieMate")
                }
            }
            if (onTasteProfileClick != null) {
                IconButton(onClick = onTasteProfileClick) {
                    Icon(Icons.Filled.Insights, contentDescription = "Your Taste")
                }
            }
            IconButton(onClick = onSearchClick) {
                Icon(Icons.Filled.Search, contentDescription = "Search")
            }
        },
    )
}
