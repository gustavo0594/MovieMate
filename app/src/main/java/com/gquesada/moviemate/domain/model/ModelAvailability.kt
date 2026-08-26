package com.gquesada.moviemate.domain.model

/**
 * Readiness of the on-device ML Kit Prompt API model (design doc &sect;07), exposed so the UI
 * can explain why "Surprise Me" is slow (first-run download) or why it silently fell back to
 * the heuristic ranker (device doesn't support the on-device model).
 */
sealed interface ModelAvailability {
    data object Unknown : ModelAvailability
    data object Checking : ModelAvailability
    data class Downloading(val bytesDownloaded: Long, val totalBytes: Long?) : ModelAvailability
    data object Unsupported : ModelAvailability
    data object Ready : ModelAvailability
    data object Error : ModelAvailability
}
