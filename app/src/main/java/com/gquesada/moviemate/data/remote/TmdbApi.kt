package com.gquesada.moviemate.data.remote

import com.gquesada.moviemate.data.remote.dto.CreditsDto
import com.gquesada.moviemate.data.remote.dto.MovieDetailDto
import com.gquesada.moviemate.data.remote.dto.MovieDto
import com.gquesada.moviemate.data.remote.dto.PagedResponseDto
import com.gquesada.moviemate.data.remote.dto.VideosResponseDto
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/** TMDB API v3 surface actually used by the app (see design doc &sect;3 for the full capability list). */
interface TmdbApi {

    @GET("movie/popular")
    suspend fun getPopularMovies(@Query("page") page: Int = 1): PagedResponseDto<MovieDto>

    @GET("movie/top_rated")
    suspend fun getTopRatedMovies(@Query("page") page: Int = 1): PagedResponseDto<MovieDto>

    @GET("movie/now_playing")
    suspend fun getNowPlayingMovies(@Query("page") page: Int = 1): PagedResponseDto<MovieDto>

    @GET("movie/upcoming")
    suspend fun getUpcomingMovies(@Query("page") page: Int = 1): PagedResponseDto<MovieDto>

    @GET("search/movie")
    suspend fun searchMovies(
        @Query("query") query: String,
        @Query("page") page: Int = 1,
    ): PagedResponseDto<MovieDto>

    @GET("movie/{movie_id}")
    suspend fun getMovieDetail(@Path("movie_id") movieId: Int): MovieDetailDto

    @GET("movie/{movie_id}/credits")
    suspend fun getCredits(@Path("movie_id") movieId: Int): CreditsDto

    @GET("movie/{movie_id}/videos")
    suspend fun getVideos(@Path("movie_id") movieId: Int): VideosResponseDto

    @GET("movie/{movie_id}/similar")
    suspend fun getSimilarMovies(@Path("movie_id") movieId: Int): PagedResponseDto<MovieDto>

    companion object {
        const val BASE_URL = "https://api.themoviedb.org/3/"
        const val IMAGE_BASE_URL = "https://image.tmdb.org/t/p/"
    }
}
