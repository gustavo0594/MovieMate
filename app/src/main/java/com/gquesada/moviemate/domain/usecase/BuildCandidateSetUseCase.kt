package com.gquesada.moviemate.domain.usecase

import com.gquesada.moviemate.domain.model.Movie
import com.gquesada.moviemate.domain.model.TasteSignals
import com.gquesada.moviemate.domain.repository.MovieRepository
import com.gquesada.moviemate.domain.repository.UserMovieRepository
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
        val cachedCatalog = movieRepository.getCachedMovies()

        val candidates = (userHistoryMovies + cachedCatalog)
            .distinctBy { it.tmdbId }
            .take(maxCandidates)

        return candidates to signals
    }

    private companion object {
        const val MAX_CANDIDATES = 1000
    }
}
