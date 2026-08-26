package com.gquesada.moviemate.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Closes the feedback loop (design doc &sect;27): not in the original product spec, added
 * because the loop needs a record of what was recommended to learn from later.
 */
@Entity(tableName = "recommendation_log")
data class RecommendationLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val movieId: Int,
    val reason: String,
    val matchScore: Int,
    val generatedAt: Long,
    val wasAccepted: Boolean? = null,
)
