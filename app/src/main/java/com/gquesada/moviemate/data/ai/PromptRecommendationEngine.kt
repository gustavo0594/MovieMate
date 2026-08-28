package com.gquesada.moviemate.data.ai

import com.google.mlkit.genai.prompt.Generation
import com.gquesada.moviemate.domain.model.ModelAvailability
import com.gquesada.moviemate.domain.model.Movie
import com.gquesada.moviemate.domain.model.Recommendation
import com.gquesada.moviemate.domain.model.TasteSignals
import kotlinx.coroutines.flow.StateFlow
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
 * [FallbackHeuristicRanker] instead of leaving "Surprise Me" stuck. [modelAvailability] tracks
 * *why*, so the UI can show a download progress bar or an "unsupported device" message instead
 * of a silent switch to the fallback ranker.
 */
class PromptRecommendationEngine : RecommendationEngine {

    private val json = Json { ignoreUnknownKeys = true }
    private val readiness = PromptModelReadiness()

    val modelAvailability: StateFlow<ModelAvailability> = readiness.modelAvailability

    override suspend fun recommend(candidates: List<Movie>, signals: TasteSignals, userMood: String?): Recommendation? {
        val model = Generation.getClient()
        return try {
            if (!readiness.ensureModelReady(model)) return null

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
            readiness.markError()
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
        val favoritePeople = topPeople(signals)
        if (favoritePeople.isNotEmpty()) {
            appendLine("Favorite directors/actors (by frequency in watched+favorites): ${favoritePeople.joinToString(", ")}")
        }
        appendLine()
        appendLine("CANDIDATES (id | title (year) | genres | director | TMDB rating)")
        candidates.forEach { movie ->
            val year = movie.releaseDate.take(4)
            val director = movie.directors.firstOrNull() ?: "unknown"
            appendLine(
                "${movie.tmdbId} | ${movie.title} ($year) | ${movie.genres.joinToString("/")} | " +
                    "$director | ${movie.voteAverage}",
            )
        }
    }

    /** Top directors/cast by frequency across watched+favorites (design doc &sect;13). */
    private fun topPeople(signals: TasteSignals): List<String> {
        val counts = mutableMapOf<String, Int>()
        val allMovies = signals.watchedWithRatings.map { it.movie } + signals.favoriteMovies
        allMovies.forEach { movie ->
            (movie.directors + movie.cast).forEach { person -> counts[person] = (counts[person] ?: 0) + 1 }
        }
        return counts.entries.sortedByDescending { it.value }.take(TOP_PEOPLE_COUNT).map { it.key }
    }

    private companion object {
        const val MAX_CANDIDATES_IN_PROMPT = 120
        const val TOP_PEOPLE_COUNT = 3
    }
}
