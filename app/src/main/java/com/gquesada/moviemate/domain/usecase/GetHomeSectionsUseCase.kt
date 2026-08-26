package com.gquesada.moviemate.domain.usecase

import com.gquesada.moviemate.domain.model.HomeSection
import com.gquesada.moviemate.domain.model.HomeSectionType
import com.gquesada.moviemate.domain.repository.MovieRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

/** Fetches the small, fixed set of TMDB rows Home shows (design doc &sect;06), concurrently. */
class GetHomeSectionsUseCase(
    private val movieRepository: MovieRepository,
) {
    suspend operator fun invoke(): List<HomeSection> = coroutineScope {
        val popular = async { movieRepository.getPopularMovies() }
        val topRated = async { movieRepository.getTopRatedMovies() }
        val nowPlaying = async { movieRepository.getNowPlayingMovies() }
        val upcoming = async { movieRepository.getUpcomingMovies() }

        listOf(
            HomeSection(HomeSectionType.POPULAR, popular.await()),
            HomeSection(HomeSectionType.TOP_RATED, topRated.await()),
            HomeSection(HomeSectionType.NOW_PLAYING, nowPlaying.await()),
            HomeSection(HomeSectionType.UPCOMING, upcoming.await()),
        )
    }
}
