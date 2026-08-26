package com.gquesada.moviemate.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class CreditsDto(
    val cast: List<CastMemberDto> = emptyList(),
    val crew: List<CrewMemberDto> = emptyList(),
)

@Serializable
data class CastMemberDto(
    val id: Int,
    val name: String,
    val order: Int = Int.MAX_VALUE,
)

@Serializable
data class CrewMemberDto(
    val id: Int,
    val name: String,
    val job: String,
)
