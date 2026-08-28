package com.gquesada.moviemate.data.ai

import com.google.mlkit.genai.common.DownloadStatus
import com.google.mlkit.genai.common.FeatureStatus
import com.google.mlkit.genai.prompt.GenerativeModel
import com.gquesada.moviemate.domain.model.ModelAvailability
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect

/**
 * Shared ML Kit GenAI Prompt API readiness/download tracking (design doc &sect;07). Every
 * on-device prompt engine ([PromptRecommendationEngine], [ConversationalPromptEngine]) owns its
 * own instance -- readiness is tracked per entry point rather than globally, since a user might
 * hit "Surprise Me" or the Assistant first, and each should show its own accurate status instead
 * of borrowing a stale one from the other.
 */
class PromptModelReadiness {

    private val _modelAvailability = MutableStateFlow<ModelAvailability>(ModelAvailability.Unknown)
    val modelAvailability: StateFlow<ModelAvailability> = _modelAvailability.asStateFlow()

    /**
     * Resolves [model]'s current status, downloading it first if needed, and updates
     * [modelAvailability] along the way so a slow first-run download or an unsupported
     * device (status [FeatureStatus.UNAVAILABLE]) is visible to the UI instead of just
     * a generic spinner.
     */
    suspend fun ensureModelReady(model: GenerativeModel): Boolean {
        _modelAvailability.value = ModelAvailability.Checking
        when (model.checkStatus()) {
            FeatureStatus.AVAILABLE -> {
                _modelAvailability.value = ModelAvailability.Ready
                return true
            }
            FeatureStatus.DOWNLOADABLE, FeatureStatus.DOWNLOADING -> {
                awaitDownload(model)
                val ready = model.checkStatus() == FeatureStatus.AVAILABLE
                _modelAvailability.value = if (ready) ModelAvailability.Ready else ModelAvailability.Error
                return ready
            }
            FeatureStatus.UNAVAILABLE -> {
                _modelAvailability.value = ModelAvailability.Unsupported
                return false
            }
            else -> {
                _modelAvailability.value = ModelAvailability.Error
                return false
            }
        }
    }

    fun markError() {
        _modelAvailability.value = ModelAvailability.Error
    }

    private suspend fun awaitDownload(model: GenerativeModel) {
        var totalBytes: Long? = null
        model.download().collect { status ->
            _modelAvailability.value = when (status) {
                is DownloadStatus.DownloadStarted -> {
                    totalBytes = status.bytesToDownload
                    ModelAvailability.Downloading(0L, totalBytes)
                }
                is DownloadStatus.DownloadProgress -> ModelAvailability.Downloading(status.totalBytesDownloaded, totalBytes)
                DownloadStatus.DownloadCompleted -> ModelAvailability.Ready
                is DownloadStatus.DownloadFailed -> ModelAvailability.Error
            }
        }
    }
}
