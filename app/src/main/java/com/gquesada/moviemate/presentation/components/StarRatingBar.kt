package com.gquesada.moviemate.presentation.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/** The 1-5 star personal rating control (design doc &sect;12) -- distinct from TMDB's own rating. */
@Composable
fun StarRatingBar(
    rating: Int,
    onRatingSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(modifier = modifier) {
        for (star in 1..5) {
            Icon(
                imageVector = if (star <= rating) Icons.Filled.Star else Icons.Outlined.StarOutline,
                contentDescription = "$star star${if (star > 1) "s" else ""}",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.clickable { onRatingSelected(star) },
            )
        }
    }
}
