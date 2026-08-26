package com.gquesada.moviemate.data.remote

import com.gquesada.moviemate.BuildConfig
import okhttp3.Interceptor
import okhttp3.Response

/** Attaches the TMDB v3 Read Access Token, sourced from local.properties -- see &sect;28. */
class TmdbAuthInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val authenticated = chain.request().newBuilder()
            .addHeader("Authorization", "Bearer ${BuildConfig.TMDB_READ_ACCESS_TOKEN}")
            .addHeader("Accept", "application/json")
            .build()
        return chain.proceed(authenticated)
    }
}
