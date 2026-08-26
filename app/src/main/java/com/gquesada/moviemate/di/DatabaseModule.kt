package com.gquesada.moviemate.di

import androidx.room.Room
import com.gquesada.moviemate.data.local.MovieMateDatabase
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val databaseModule = module {
    single {
        Room.databaseBuilder(
            androidContext(),
            MovieMateDatabase::class.java,
            MovieMateDatabase.DATABASE_NAME,
        ).build()
    }
    single { get<MovieMateDatabase>().movieDao() }
    single { get<MovieMateDatabase>().userMovieStateDao() }
    single { get<MovieMateDatabase>().recommendationLogDao() }
}
