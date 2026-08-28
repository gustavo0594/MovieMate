package com.gquesada.moviemate.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.gquesada.moviemate.data.local.entity.RecommendationLogEntity

@Dao
interface RecommendationLogDao {
    @Insert
    suspend fun insert(entry: RecommendationLogEntity): Long

    @Query("SELECT movieId FROM recommendation_log ORDER BY generatedAt DESC LIMIT :limit")
    suspend fun getRecentlyRecommendedIds(limit: Int = 20): List<Int>

    /**
     * Recommendations the user never acted on (favorited/watchlisted/watched) -- the negative
     * half of the feedback loop (design doc &sect;27). `OFFSET 1` skips the single most-recent
     * entry, since that one may still be awaiting user action rather than truly ignored.
     */
    @Query(
        "SELECT movieId FROM recommendation_log WHERE wasAccepted IS NULL " +
            "ORDER BY generatedAt DESC LIMIT :limit OFFSET 1",
    )
    suspend fun getDeclinedMovieIds(limit: Int = 20): List<Int>

    @Query("UPDATE recommendation_log SET wasAccepted = 1 WHERE movieId = :movieId AND wasAccepted IS NULL")
    suspend fun markAccepted(movieId: Int)
}
