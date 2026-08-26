package com.gquesada.moviemate.domain.usecase

import com.gquesada.moviemate.domain.model.HomeSection
import com.gquesada.moviemate.domain.model.HomeSectionType
import com.gquesada.moviemate.domain.repository.MovieRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

/** Fetches the small, fixed set of rows Home shows (design doc &sect;06), concurrently. */
class GetHomeSectionsUseCase(
    private val movieRepository: MovieRepository,
    private val getPickedForYou: GetPickedForYouUseCase,
) {
    suspend operator fun invoke(): List<HomeSection> = coroutineScope {
        val popular = async { movieRepository.getPopularMovies() }
        val topRated = async { movieRepository.getTopRatedMovies() }
        val nowPlaying = async { movieRepository.getNowPlayingMovies() }
        val upcoming = async { movieRepository.getUpcomingMovies() }

        val tmdbSections = listOf(
            HomeSection(HomeSectionType.POPULAR, popular.await()),
            HomeSection(HomeSectionType.TOP_RATED, topRated.await()),
            HomeSection(HomeSectionType.NOW_PLAYING, nowPlaying.await()),
            HomeSection(HomeSectionType.UPCOMING, upcoming.await()),
        )

        // Runs after the rows above so it reuses the catalog they just cached, rather than
        // triggering its own duplicate TMDB fetches on a cold start.
        val pickedForYou = runCatching { getPickedForYou() }.getOrDefault(emptyList())

        if (pickedForYou.isEmpty()) {
            tmdbSections
        } else {
            listOf(HomeSection(HomeSectionType.PICKED_FOR_YOU, pickedForYou)) + tmdbSections
        }
    }
}
