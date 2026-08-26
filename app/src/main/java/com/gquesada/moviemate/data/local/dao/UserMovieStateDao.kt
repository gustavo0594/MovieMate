package com.gquesada.moviemate.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.gquesada.moviemate.data.local.entity.UserMovieStateEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserMovieStateDao {
    @Upsert
    suspend fun upsert(state: UserMovieStateEntity)

    @Query("SELECT * FROM user_movie_state WHERE movieId = :movieId")
    suspend fun getState(movieId: Int): UserMovieStateEntity?

    @Query("SELECT * FROM user_movie_state WHERE movieId = :movieId")
    fun observeState(movieId: Int): Flow<UserMovieStateEntity?>

    @Query("SELECT * FROM user_movie_state WHERE isFavorite = 1")
    fun observeFavorites(): Flow<List<UserMovieStateEntity>>

    @Query("SELECT * FROM user_movie_state WHERE isWatched = 1")
    fun observeWatched(): Flow<List<UserMovieStateEntity>>

    @Query("SELECT * FROM user_movie_state WHERE isInWatchlist = 1")
    fun observeWatchlist(): Flow<List<UserMovieStateEntity>>
}
