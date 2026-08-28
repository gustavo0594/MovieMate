package com.gquesada.moviemate.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.gquesada.moviemate.domain.model.ModelAvailability

/**
 * Always-on status strip for the on-device ML Kit GenAI model, meant to sit directly under a
 * screen's [androidx.compose.material3.TopAppBar] so the user always knows, at a glance, whether
 * the on-device AI is available, downloading, or unavailable -- instead of that only being
 * inferable from a transient loading spinner.
 */
@Composable
fun ModelStatusBanner(modelAvailability: ModelAvailability, modifier: Modifier = Modifier) {
    val style = modelAvailability.statusStyle()
    Column(modifier = modifier.fillMaxWidth().background(style.background)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(style.icon, contentDescription = null, tint = style.content, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text(
                style.label,
                style = MaterialTheme.typography.labelLarge,
                color = style.content,
                modifier = Modifier.weight(1f),
            )
            style.percentLabel?.let { percentLabel ->
                Text(percentLabel, style = MaterialTheme.typography.labelLarge, color = style.content)
            }
        }
        if (modelAvailability is ModelAvailability.Downloading) {
            val total = modelAvailability.totalBytes
            if (total != null && total > 0) {
                LinearProgressIndicator(
                    progress = { (modelAvailability.bytesDownloaded.toFloat() / total).coerceIn(0f, 1f) },
                    modifier = Modifier.fillMaxWidth(),
                    color = style.content,
                    trackColor = style.background,
                )
            } else {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth(), color = style.content, trackColor = style.background)
            }
        }
    }
}

private class StatusStyle(
    val background: Color,
    val content: Color,
    val icon: ImageVector,
    val label: String,
    val percentLabel: String? = null,
)

@Composable
private fun ModelAvailability.statusStyle(): StatusStyle = when (this) {
    ModelAvailability.Ready -> StatusStyle(
        background = MaterialTheme.colorScheme.primaryContainer,
        content = MaterialTheme.colorScheme.onPrimaryContainer,
        icon = Icons.Filled.CheckCircle,
        label = "On-device AI available",
    )
    is ModelAvailability.Downloading -> {
        val total = totalBytes
        val percent = if (total != null && total > 0) (bytesDownloaded * 100 / total).coerceIn(0, 100) else null
        StatusStyle(
            background = MaterialTheme.colorScheme.tertiaryContainer,
            content = MaterialTheme.colorScheme.onTertiaryContainer,
            icon = Icons.Filled.CloudDownload,
            label = "Downloading on-device AI model…",
            percentLabel = percent?.let { "$it%" },
        )
    }
    ModelAvailability.Checking -> StatusStyle(
        background = MaterialTheme.colorScheme.surfaceVariant,
        content = MaterialTheme.colorScheme.onSurfaceVariant,
        icon = Icons.Filled.HourglassEmpty,
        label = "Checking on-device AI availability…",
    )
    ModelAvailability.Unsupported -> StatusStyle(
        background = MaterialTheme.colorScheme.errorContainer,
        content = MaterialTheme.colorScheme.onErrorContainer,
        icon = Icons.Filled.WarningAmber,
        label = "On-device AI unavailable on this device",
    )
    ModelAvailability.Error -> StatusStyle(
        background = MaterialTheme.colorScheme.errorContainer,
        content = MaterialTheme.colorScheme.onErrorContainer,
        icon = Icons.Filled.ErrorOutline,
        label = "On-device AI unavailable right now",
    )
    ModelAvailability.Unknown -> StatusStyle(
        background = MaterialTheme.colorScheme.surfaceVariant,
        content = MaterialTheme.colorScheme.onSurfaceVariant,
        icon = Icons.Filled.HourglassEmpty,
        label = "On-device AI status unknown",
    )
}
