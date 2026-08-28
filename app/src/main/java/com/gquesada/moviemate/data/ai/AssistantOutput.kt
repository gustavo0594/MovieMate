package com.gquesada.moviemate.data.ai

import kotlinx.serialization.Serializable

/**
 * The JSON shape the Assistant prompt asks the model for (design doc &sect;17), mirroring
 * [RecommendationOutput]'s "parse then validate against candidates" pattern. [movieId] is
 * nullable because not every turn recommends a movie -- a clarifying question shouldn't force
 * one.
 */
@Serializable
data class AssistantOutput(
    val reply: String,
    val movieId: Int? = null,
    val matchScore: Int? = null,
)
