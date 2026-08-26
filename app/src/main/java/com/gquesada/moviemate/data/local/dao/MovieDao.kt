package com.gquesada.moviemate.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.gquesada.moviemate.data.local.entity.MovieEntity

@Dao
interface MovieDao {
    @Upsert
    suspend fun upsertAll(movies: List<MovieEntity>)

    @Upsert
    suspend fun upsert(movie: MovieEntity)

    @Query("SELECT * FROM movies WHERE tmdbId = :movieId")
    suspend fun getById(movieId: Int): MovieEntity?

    @Query("SELECT * FROM movies ORDER BY cachedAt DESC LIMIT :limit")
    suspend fun getCached(limit: Int = 2000): List<MovieEntity>
}
