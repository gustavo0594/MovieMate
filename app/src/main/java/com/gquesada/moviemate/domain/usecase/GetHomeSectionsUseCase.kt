package com.gquesada.moviemate.domain.usecase

import com.gquesada.moviemate.domain.model.HomeSection
import com.gquesada.moviemate.domain.model.HomeSectionType
import com.gquesada.moviemate.domain.model.HomeSections
import com.gquesada.moviemate.domain.repository.MovieRepository
import com.gquesada.moviemate.domain.repository.RecommendationRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

/** Fetches the small, fixed set of rows Home shows (design doc &sect;06), concurrently. */
class GetHomeSectionsUseCase(
    private val movieRepository: MovieRepository,
    private val buildCandidateSet: BuildCandidateSetUseCase,
    private val recommendationRepository: RecommendationRepository,
) {
    suspend operator fun invoke(): HomeSections = coroutineScope {
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

        // Built once, after the rows above so it reuses the catalog they just cached, and
        // shared by both the live top pick and "Picked for You" so Home doesn't pay for
        // candidate-set assembly (including similar-movies lookups) twice.
        val candidateSet = runCatching { buildCandidateSet() }.getOrNull()
        val candidates = candidateSet?.first ?: emptyList()
        val signals = candidateSet?.second

        val tonightsPick = if (candidates.isNotEmpty() && signals != null) {
            runCatching { recommendationRepository.tonightsPick(candidates, signals) }.getOrNull()
        } else {
            null
        }
        val pickedForYou = if (candidates.isNotEmpty() && signals != null) {
            runCatching { recommendationRepository.pickForYou(candidates, signals, PICKED_FOR_YOU_COUNT) }.getOrDefault(emptyList())
        } else {
            emptyList()
        }

        val sections = if (pickedForYou.isEmpty()) {
            tmdbSections
        } else {
            listOf(HomeSection(HomeSectionType.PICKED_FOR_YOU, pickedForYou)) + tmdbSections
        }

        HomeSections(tonightsPick = tonightsPick, sections = sections)
    }

    private companion object {
        const val PICKED_FOR_YOU_COUNT = 10
    }
}
