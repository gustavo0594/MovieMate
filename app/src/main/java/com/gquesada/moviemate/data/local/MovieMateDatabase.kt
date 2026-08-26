package com.gquesada.moviemate.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.gquesada.moviemate.data.local.dao.MovieDao
import com.gquesada.moviemate.data.local.dao.RecommendationLogDao
import com.gquesada.moviemate.data.local.dao.UserMovieStateDao
import com.gquesada.moviemate.data.local.entity.MovieEntity
import com.gquesada.moviemate.data.local.entity.RecommendationLogEntity
import com.gquesada.moviemate.data.local.entity.UserMovieStateEntity

@Database(
    entities = [MovieEntity::class, UserMovieStateEntity::class, RecommendationLogEntity::class],
    version = 1,
    exportSchema = true,
)
@TypeConverters(Converters::class)
abstract class MovieMateDatabase : RoomDatabase() {
    abstract fun movieDao(): MovieDao
    abstract fun userMovieStateDao(): UserMovieStateDao
    abstract fun recommendationLogDao(): RecommendationLogDao

    companion object {
        const val DATABASE_NAME = "moviemate.db"
    }
}
