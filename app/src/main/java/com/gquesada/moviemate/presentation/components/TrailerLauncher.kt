package com.gquesada.moviemate.presentation.components

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import androidx.core.net.toUri

/**
 * Opens a TMDB trailer key in the YouTube app, falling back to a browser.
 * A scaffold-stage choice over embedding ExoPlayer (design doc &sect;19 leaves the
 * exact playback mechanism open) -- inline in-app playback is a natural P1 upgrade.
 */
fun openTrailer(context: Context, youtubeKey: String) {
    val uri = "https://www.youtube.com/watch?v=$youtubeKey".toUri()
    try {
        context.startActivity(Intent(Intent.ACTION_VIEW, uri).setPackage("com.google.android.youtube"))
    } catch (_: ActivityNotFoundException) {
        context.startActivity(Intent(Intent.ACTION_VIEW, uri))
    }
}
