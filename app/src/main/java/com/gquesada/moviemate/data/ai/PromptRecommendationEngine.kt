package com.gquesada.moviemate.data.ai

import com.google.mlkit.genai.common.FeatureStatus
import com.google.mlkit.genai.prompt.Generation
import com.gquesada.moviemate.domain.model.Movie
import com.gquesada.moviemate.domain.model.Recommendation
import com.gquesada.moviemate.domain.model.TasteSignals
import kotlinx.coroutines.flow.collect
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

/**
 * On-device recommendation via ML Kit's GenAI Prompt API (Gemini Nano). The model is asked
 * to reply with a single JSON object, which is parsed and then validated against the
 * candidate set (design doc &sect;07) -- an id outside that set, or a response that doesn't
 * parse at all, makes this return null rather than ever inventing a movie.
 *
 * The API is beta as of this writing -- if `com.google.mlkit.genai.prompt.Generation` or
 * `com.google.mlkit.genai.common.FeatureStatus` have moved, Android Studio's auto-import
 * will find the current location once `com.google.mlkit:genai-prompt` is on the classpath.
 *
 * [recommend] never throws to the caller: it returns null whenever the model isn't ready,
 * fails, or answers with something outside the candidate set, so
 * [com.gquesada.moviemate.data.repository.RecommendationRepositoryImpl] can fall back to
 * [FallbackHeuristicRanker] instead of leaving "Surprise Me" stuck.
 */
class PromptRecommendationEngine : RecommendationEngine {

    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun recommend(candidates: List<Movie>, signals: TasteSignals, userMood: String?): Recommendation? {
        val model = Generation.getClient()
        return try {
            when (model.checkStatus()) {
                FeatureStatus.AVAILABLE -> {}
                FeatureStatus.DOWNLOADABLE -> {
                    // Drain the download to completion; this can take a while on first run,
                    // so a real UI should surface progress instead of blocking silently.
                    model.download().collect { }
                    if (model.checkStatus() != FeatureStatus.AVAILABLE) return null
                }
                else -> return null
            }

            val genreAffinity = RecommendationScoring.genreAffinity(signals)
            val shortlisted = candidates
                .sortedByDescending { RecommendationScoring.affinityScore(it, genreAffinity) }
                .take(MAX_CANDIDATES_IN_PROMPT)

            val rawText = model.generateContent(buildPrompt(shortlisted, signals, userMood))
                .candidates
                .firstOrNull()
                ?.text
                ?: return null

            val output = parseOutput(rawText) ?: return null
            val chosen = shortlisted.firstOrNull { it.tmdbId == output.movieId } ?: return null

            Recommendation(
                movie = chosen,
                matchScore = output.matchScore.coerceIn(0, 100),
                reason = output.reason,
                fromAi = true,
            )
        } catch (t: Throwable) {
            null
        } finally {
            model.close()
        }
    }

    private fun parseOutput(rawText: String): RecommendationOutput? {
        val start = rawText.indexOf('{')
        val end = rawText.lastIndexOf('}')
        if (start == -1 || end == -1 || end < start) return null
        return runCatching { json.decodeFromString<RecommendationOutput>(rawText.substring(start, end + 1)) }.getOrNull()
    }

    private fun buildPrompt(candidates: List<Movie>, signals: TasteSignals, userMood: String?): String = buildString {
        appendLine("You are a movie recommendation assistant picking ONE movie for this user tonight.")
        appendLine("You MUST choose a movieId that appears in the CANDIDATES list below -- never invent one.")
        appendLine(
            "Respond with ONLY a single JSON object, no markdown fences, no extra text, exactly this shape: " +
                "{\"movieId\": <int>, \"matchScore\": <int 0-100>, \"reason\": \"<one or two sentences, " +
                "addressed to the user>\"}",
        )
        appendLine()
        appendLine("USER SIGNALS")
        appendLine("Watched, with personal rating out of 5 where given (personal ratings matter more than TMDB's):")
        signals.watchedWithRatings.forEach { (movie, state) ->
            val rating = state.personalRating?.let { "$it/5" } ?: "unrated"
            appendLine("- ${movie.title} (${movie.genres.joinToString("/")}) -- $rating")
        }
        if (signals.favoriteMovies.isNotEmpty()) {
            appendLine("Favorites: ${signals.favoriteMovies.joinToString(", ") { it.title }}")
        }
        if (signals.watchlistMovies.isNotEmpty()) {
            appendLine("Still wants to watch: ${signals.watchlistMovies.joinToString(", ") { it.title }}")
        }
        if (!userMood.isNullOrBlank()) {
            appendLine("Tonight's mood: \"$userMood\"")
        }
        appendLine()
        appendLine("CANDIDATES (id | title (year) | genres | TMDB rating)")
        candidates.forEach { movie ->
            val year = movie.releaseDate.take(4)
            appendLine("${movie.tmdbId} | ${movie.title} ($year) | ${movie.genres.joinToString("/")} | ${movie.voteAverage}")
        }
    }

    private companion object {
        const val MAX_CANDIDATES_IN_PROMPT = 120
    }
}
