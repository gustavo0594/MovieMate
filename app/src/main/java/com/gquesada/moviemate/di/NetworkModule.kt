package com.gquesada.moviemate.di

import com.gquesada.moviemate.BuildConfig
import com.gquesada.moviemate.data.remote.TmdbApi
import com.gquesada.moviemate.data.remote.TmdbAuthInterceptor
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.dsl.module
import retrofit2.Retrofit

val networkModule = module {
    single {
        Json {
            ignoreUnknownKeys = true
            coerceInputValues = true
        }
    }

    single { TmdbAuthInterceptor() }

    single {
        OkHttpClient.Builder()
            .addInterceptor(get<TmdbAuthInterceptor>())
            .apply {
                if (BuildConfig.DEBUG) {
                    addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BASIC))
                }
            }
            .build()
    }

    single {
        Retrofit.Builder()
            .baseUrl(TmdbApi.BASE_URL)
            .client(get())
            .addConverterFactory(get<Json>().asConverterFactory("application/json".toMediaType()))
            .build()
    }

    single { get<Retrofit>().create(TmdbApi::class.java) }
}
