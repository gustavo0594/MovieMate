package com.gquesada.moviemate.domain.model

/** One turn of the AI Movie Assistant conversation (design doc &sect;17). */
data class ChatMessage(
    val role: ChatRole,
    val text: String,
    /** Set on assistant turns that pick a specific movie, matched against the candidate set. */
    val recommendedMovie: Movie? = null,
    val matchScore: Int? = null,
)

enum class ChatRole { USER, ASSISTANT }
