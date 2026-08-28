package com.gquesada.moviemate.domain.usecase

import com.gquesada.moviemate.domain.model.Movie
import com.gquesada.moviemate.domain.model.MovieWithUserState
import com.gquesada.moviemate.domain.model.TasteSignals
import com.gquesada.moviemate.domain.repository.MovieRepository
import com.gquesada.moviemate.domain.repository.UserMovieRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.first

/**
 * Assembles the closed set of real candidate movies the Prompt API is allowed to
 * choose from (design doc &sect;14): the user's own history first, then the cached
 * TMDB catalog, capped so the prompt stays within context limits.
 */
class BuildCandidateSetUseCase(
    private val movieRepository: MovieRepository,
    private val userMovieRepository: UserMovieRepository,
) {
    suspend operator fun invoke(maxCandidates: Int = MAX_CANDIDATES): Pair<List<Movie>, TasteSignals> {
        val watched = userMovieRepository.observeWatched().first()
        val favorites = userMovieRepository.observeFavorites().first()
        val watchlist = userMovieRepository.observeWatchlist().first()

        val signals = TasteSignals(
            watchedWithRatings = watched,
            favoriteMovies = favorites.map { it.movie },
            watchlistMovies = watchlist.map { it.movie },
        )

        val userHistoryMovies = (watched.map { it.movie } + favorites.map { it.movie } + watchlist.map { it.movie })
        var cachedCatalog = movieRepository.getCachedMovies()
        if (cachedCatalog.size < MIN_CATALOG_SIZE) {
            cachedCatalog = backfillCatalog()
        }
        val similarMovies = fetchSimilarToRecent(watched, favorites)

        val candidates = (userHistoryMovies + similarMovies + cachedCatalog)
            .distinctBy { it.tmdbId }
            .take(maxCandidates)

        return candidates to signals
    }

    /**
     * Pulls TMDB's "similar movies" for a handful of the user's most recent watched/favorite
     * titles (design doc &sect;14 lists this as a candidate source) so the pool itself is
     * taste-informed, not just the ranking that runs over it afterwards.
     */
    private suspend fun fetchSimilarToRecent(
        watched: List<MovieWithUserState>,
        favorites: List<MovieWithUserState>,
    ): List<Movie> = coroutineScope {
        val seeds = (watched.map { it.movie } + favorites.map { it.movie })
            .distinctBy { it.tmdbId }
            .take(SIMILAR_SEED_COUNT)
        seeds
            .map { seed -> async { runCatching { movieRepository.getSimilarMovies(seed.tmdbId) }.getOrDefault(emptyList()) } }
            .awaitAll()
            .flatten()
    }

    /**
     * Tops up the local candidate pool toward the size design doc &sect;14 calls for
     * (500-2,000 titles) by pulling a few extra pages of TMDB's list endpoints. Without this,
     * a fresh install is limited to whatever the user has happened to scroll past on
     * Home/Search, which is nowhere near enough for the Prompt API to pick from meaningfully.
     */
    private suspend fun backfillCatalog(): List<Movie> = coroutineScope {
        (1..BACKFILL_PAGES).flatMap { page ->
            listOf(
                async { runCatching { movieRepository.getPopularMovies(page) }.getOrDefault(emptyList()) },
                async { runCatching { movieRepository.getTopRatedMovies(page) }.getOrDefault(emptyList()) },
                async { runCatching { movieRepository.getNowPlayingMovies(page) }.getOrDefault(emptyList()) },
                async { runCatching { movieRepository.getUpcomingMovies(page) }.getOrDefault(emptyList()) },
            )
        }.awaitAll()
        movieRepository.getCachedMovies()
    }

    private companion object {
        const val MAX_CANDIDATES = 1000
        const val MIN_CATALOG_SIZE = 300
        const val BACKFILL_PAGES = 3
        const val SIMILAR_SEED_COUNT = 5
    }
}
