package com.gquesada.moviemate.data.ai

import com.gquesada.moviemate.domain.model.Movie
import com.gquesada.moviemate.domain.model.TasteSignals

/**
 * Shared genre-affinity heuristic used both to shortlist candidates for the AI prompt
 * (so the prompt stays small) and to rank candidates when the fallback ranker runs.
 * Personal ratings count for more than TMDB's rating (design doc &sect;12).
 */
internal object RecommendationScoring {

    fun genreAffinity(signals: TasteSignals): Map<String, Double> {
        val raw = mutableMapOf<String, Double>()
        signals.watchedWithRatings.forEach { (movie, state) ->
            val weight = state.personalRating?.toDouble() ?: (movie.voteAverage / 2.0)
            movie.genres.forEach { genre -> raw[genre] = (raw[genre] ?: 0.0) + weight }
        }
        signals.favoriteMovies.forEach { movie ->
            movie.genres.forEach { genre -> raw[genre] = (raw[genre] ?: 0.0) + 3.0 }
        }
        val total = raw.values.sum()
        if (total <= 0.0) return emptyMap()
        return raw.mapValues { it.value / total }
    }

    /** Bounded roughly to [0, 100]: up to 50 from genre overlap, up to 10 from TMDB rating, 40 base. */
    fun affinityScore(movie: Movie, genreAffinity: Map<String, Double>): Double {
        val overlap = movie.genres.sumOf { genreAffinity[it] ?: 0.0 }.coerceIn(0.0, 1.0)
        val ratingBoost = (movie.voteAverage / 10.0).coerceIn(0.0, 1.0)
        return 40.0 + overlap * 50.0 + ratingBoost * 10.0
    }
}
