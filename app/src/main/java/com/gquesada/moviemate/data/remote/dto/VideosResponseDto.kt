package com.gquesada.moviemate.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class VideosResponseDto(
    val results: List<VideoDto> = emptyList(),
)

@Serializable
data class VideoDto(
    val key: String,
    val site: String,
    val type: String,
    val official: Boolean = false,
)
