package com.gquesada.moviemate.data.ai

import com.gquesada.moviemate.domain.model.Movie
import com.gquesada.moviemate.domain.model.Recommendation
import com.gquesada.moviemate.domain.model.TasteSignals

/**
 * Deterministic, on-device, no-model ranking. Used when [PromptRecommendationEngine]
 * is unavailable, still downloading, or returns an id outside the candidate set
 * (design doc &sect;07's validation guard) -- "Surprise Me" must never dead-end.
 */
class FallbackHeuristicRanker : RecommendationEngine {

    override suspend fun recommend(candidates: List<Movie>, signals: TasteSignals, userMood: String?): Recommendation {
        val genreAffinity = RecommendationScoring.genreAffinity(signals)
        val watchedIds = signals.watchedWithRatings.map { it.movie.tmdbId }.toSet()

        val ranked = candidates
            .filterNot { it.tmdbId in watchedIds }
            .ifEmpty { candidates }
            .sortedByDescending { RecommendationScoring.affinityScore(it, genreAffinity) }

        val topMovie = ranked.first()
        val matchScore = RecommendationScoring.affinityScore(topMovie, genreAffinity)
            .toInt()
            .coerceIn(35, 97)

        return Recommendation(
            movie = topMovie,
            matchScore = matchScore,
            reason = buildReason(genreAffinity, signals),
            fromAi = false,
        )
    }

    private fun buildReason(genreAffinity: Map<String, Double>, signals: TasteSignals): String {
        val topGenres = genreAffinity.entries.sortedByDescending { it.value }.take(2).map { it.key }
        if (topGenres.isEmpty()) {
            return "A well-reviewed pick to get started -- rate a few movies and recommendations will get more personal."
        }
        val genreText = topGenres.joinToString(" and ")
        val highlyRatedTitles = signals.watchedWithRatings
            .filter { (it.userState.personalRating ?: 0) >= 4 }
            .map { it.movie.title }
            .take(2)
        return if (highlyRatedTitles.isNotEmpty()) {
            "You rated ${highlyRatedTitles.joinToString(" and ")} highly, and you tend to enjoy $genreText."
        } else {
            "Based on your favorites, you seem to enjoy $genreText."
        }
    }
}
