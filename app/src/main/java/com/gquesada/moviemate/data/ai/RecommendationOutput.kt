package com.gquesada.moviemate.data.ai

import kotlinx.serialization.Serializable

/**
 * The JSON shape the prompt asks the model for (design doc &sect;07). Parsed with plain
 * kotlinx.serialization -- [movieId] is still checked against the candidate set afterwards,
 * since the model can't be trusted not to invent one.
 */
@Serializable
data class RecommendationOutput(
    val movieId: Int,
    val matchScore: Int,
    val reason: String,
)
