package com.gquesada.moviemate.data.ai

import com.google.mlkit.genai.prompt.Generation
import com.gquesada.moviemate.domain.model.ChatMessage
import com.gquesada.moviemate.domain.model.ChatRole
import com.gquesada.moviemate.domain.model.ModelAvailability
import com.gquesada.moviemate.domain.model.Movie
import com.gquesada.moviemate.domain.model.TasteSignals
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

/**
 * Conversational recommendation via ML Kit's GenAI Prompt API (design doc &sect;17). The real
 * `genai-prompt:1.0.0-beta2` artifact has no chat-session type (see project memory on its
 * confirmed API shape) -- only a stateless `generateContent(prompt: String)` call -- so
 * multi-turn "memory" is simulated by resending prior turns as text in each prompt, the same
 * way [PromptRecommendationEngine] resends [TasteSignals] on every call.
 */
class ConversationalPromptEngine {

    private val json = Json { ignoreUnknownKeys = true }
    private val readiness = PromptModelReadiness()

    val modelAvailability: StateFlow<ModelAvailability> = readiness.modelAvailability

    suspend fun sendMessage(
        history: List<ChatMessage>,
        newUserMessage: String,
        candidates: List<Movie>,
        signals: TasteSignals,
    ): ChatMessage? {
        val model = Generation.getClient()
        return try {
            if (!readiness.ensureModelReady(model)) return null

            val genreAffinity = RecommendationScoring.genreAffinity(signals)
            val shortlisted = candidates
                .sortedByDescending { RecommendationScoring.affinityScore(it, genreAffinity) }
                .take(MAX_CANDIDATES_IN_PROMPT)

            val rawText = model.generateContent(buildPrompt(shortlisted, signals, history, newUserMessage))
                .candidates
                .firstOrNull()
                ?.text
                ?: return null

            val output = parseOutput(rawText) ?: return null
            val chosen = output.movieId?.let { id -> shortlisted.firstOrNull { it.tmdbId == id } }

            ChatMessage(
                role = ChatRole.ASSISTANT,
                text = output.reply,
                recommendedMovie = chosen,
                matchScore = chosen?.let { output.matchScore?.coerceIn(0, 100) },
            )
        } catch (t: Throwable) {
            readiness.markError()
            null
        } finally {
            model.close()
        }
    }

    private fun parseOutput(rawText: String): AssistantOutput? {
        val start = rawText.indexOf('{')
        val end = rawText.lastIndexOf('}')
        if (start == -1 || end == -1 || end < start) return null
        return runCatching { json.decodeFromString<AssistantOutput>(rawText.substring(start, end + 1)) }.getOrNull()
    }

    private fun buildPrompt(
        candidates: List<Movie>,
        signals: TasteSignals,
        history: List<ChatMessage>,
        newUserMessage: String,
    ): String = buildString {
        appendLine("You are MovieMate's conversational movie assistant, chatting with a user about what to watch.")
        appendLine(
            "You MUST choose a movieId that appears in the CANDIDATES list below when you recommend a movie -- " +
                "never invent one. You don't have to recommend a movie every turn -- you can ask a clarifying " +
                "question instead and leave movieId null.",
        )
        appendLine(
            "Respond with ONLY a single JSON object, no markdown fences, no extra text, exactly this shape: " +
                "{\"reply\": \"<your conversational reply, one to three sentences>\", " +
                "\"movieId\": <int or null>, \"matchScore\": <int 0-100 or null>}",
        )
        appendLine()
        appendLine("USER SIGNALS")
        appendLine("Watched, with personal rating out of 5 where given:")
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
        appendLine()
        if (history.isNotEmpty()) {
            appendLine("CONVERSATION SO FAR")
            history.forEach { message ->
                val speaker = if (message.role == ChatRole.USER) "User" else "Assistant"
                appendLine("$speaker: ${message.text}")
            }
            appendLine()
        }
        appendLine("User: $newUserMessage")
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

    private companion object {
        const val MAX_CANDIDATES_IN_PROMPT = 120
    }
}
