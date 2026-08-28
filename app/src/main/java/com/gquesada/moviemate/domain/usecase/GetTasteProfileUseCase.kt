package com.gquesada.moviemate.domain.usecase

import com.gquesada.moviemate.domain.model.GenreShare
import com.gquesada.moviemate.domain.model.TasteProfile
import com.gquesada.moviemate.domain.repository.UserMovieRepository
import kotlinx.coroutines.flow.first

/**
 * Movie Taste Profile (design doc &sect;18): entirely derived from local watch history and
 * ratings, not stored as a static preference set.
 */
class GetTasteProfileUseCase(
    private val userMovieRepository: UserMovieRepository,
) {
    suspend operator fun invoke(): TasteProfile {
        val watched = userMovieRepository.observeWatched().first()
        val favorites = userMovieRepository.observeFavorites().first()

        val genreWeights = mutableMapOf<String, Double>()
        watched.forEach { (movie, state) ->
            val weight = state.personalRating?.toDouble() ?: 1.0
            movie.genres.forEach { genre -> genreWeights[genre] = (genreWeights[genre] ?: 0.0) + weight }
        }
        favorites.forEach { (movie, _) ->
            movie.genres.forEach { genre -> genreWeights[genre] = (genreWeights[genre] ?: 0.0) + 1.0 }
        }
        val total = genreWeights.values.sum()
        val topGenres = genreWeights.entries
            .sortedByDescending { it.value }
            .take(TOP_GENRE_COUNT)
            .map { GenreShare(it.key, if (total > 0.0) it.value / total else 0.0) }

        val ratings = watched.mapNotNull { it.userState.personalRating }

        return TasteProfile(
            topGenres = topGenres,
            averagePersonalRating = if (ratings.isNotEmpty()) ratings.average() else null,
            watchedCount = watched.size,
            favoritesCount = favorites.size,
        )
    }

    private companion object {
        const val TOP_GENRE_COUNT = 5
    }
}
