package com.gquesada.moviemate.presentation.components

import androidx.compose.material.icons.Icons
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
fun PrimaryTopBar(title: String, onSearchClick: () -> Unit) {
    TopAppBar(
        title = { Text(title) },
        actions = {
            IconButton(onClick = onSearchClick) {
                Icon(Icons.Filled.Search, contentDescription = "Search")
            }
        },
    )
}
