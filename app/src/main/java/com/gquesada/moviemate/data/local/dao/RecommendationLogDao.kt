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
}
