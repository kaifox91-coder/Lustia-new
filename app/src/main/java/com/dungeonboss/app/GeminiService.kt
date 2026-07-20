package com.dungeonboss.app

import android.util.Log
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import java.io.IOException
import kotlin.math.min
import kotlin.random.Random

data class ChatMessage(
    val role: String,
    val content: String
)

class GeminiService(apiKey: String = BuildConfig.GEMINI_API_KEY) {
    companion object {
        private const val TAG = "GeminiService"
        private const val REQUEST_TIMEOUT_MS = 45_000L
        private const val MAX_ATTEMPTS = 6
        private const val BASE_BACKOFF_MS = 1_000L
        private const val MAX_BACKOFF_MS = 8_000L
    }

    private val model = GenerativeModel(
        modelName = "gemini-1.5-flash",
        apiKey = apiKey
    )

    private val conversationHistory = mutableListOf<ChatMessage>()

    suspend fun initializeDungeon(boss: Boss): String = withContext(Dispatchers.IO) {
        val systemPrompt = buildSystemPrompt(boss)
        conversationHistory.clear()

        val response = runCatching {
            val prompt = systemPrompt + "\n\nGreet the boss and set the scene for the first time. Describe the newly constructed Succubus Floor—emphasizing a strictly SFW, deeply comfy, and cutesy atmosphere washed in soft purple and red hues, filled with lavishly plush furnishings, warm velvet drapes, oversized crimson floor pillows, and cute neon-violet ambient glow integrated into the ancient stone structure."
            val content = content { text(prompt) }
            generateWithRetry(content, requestLabel = "initializeDungeon")
        }.getOrElse { throwable ->
            Log.e(TAG, "initializeDungeon failed after retries: ${throwable::class.simpleName}: ${throwable.message}")
            fallbackMessage(isInit = true)
        }

        conversationHistory.add(ChatMessage("dungeon", response))
        response
    }

    suspend fun chat(userMessage: String, boss: Boss): String = withContext(Dispatchers.IO) {
        conversationHistory.add(ChatMessage("user", userMessage))

        val systemPrompt = buildSystemPrompt(boss)
        val historyContext = buildConversationContext()

        val dungeonResponse = runCatching {
            val fullPrompt = """
$systemPrompt

CONVERSATION HISTORY:
$historyContext

USER: $userMessage

DUNGEON RESPONSE:
            """.trimIndent()

            val content = content {
                text(fullPrompt)
            }
            generateWithRetry(content, requestLabel = "chat")
        }.getOrElse { throwable ->
            Log.e(TAG, "chat failed after retries: ${throwable::class.simpleName}: ${throwable.message}")
            fallbackMessage(isInit = false)
        }

        conversationHistory.add(ChatMessage("dungeon", dungeonResponse))
        dungeonResponse
    }

    private suspend fun generateWithRetry(
        promptContent: com.google.ai.client.generativeai.type.Content,
        requestLabel: String
    ): String {
        var lastError: Throwable? = null

        for (attempt in 1..MAX_ATTEMPTS) {
            try {
                val result = withTimeout(REQUEST_TIMEOUT_MS) {
                    model.generateContent(promptContent)
                }
                val text = result.text?.takeIf { it.isNotBlank() } ?: "The stone remains silent."
                if (attempt > 1) {
                    Log.i(TAG, "$requestLabel succeeded on retry attempt=$attempt")
                }
                return text
            } catch (t: Throwable) {
                lastError = t
                val retryable = isRetryable(t)
                val shouldRetry = retryable && attempt < MAX_ATTEMPTS

                Log.w(
                    TAG,
                    "$requestLabel failed attempt=$attempt/$MAX_ATTEMPTS retryable=$retryable errorType=${t::class.simpleName} message=${t.message}"
                )

                if (!shouldRetry) break

                val backoff = computeBackoffWithJitter(attempt)
                Log.i(TAG, "$requestLabel retrying in ${backoff}ms (attempt ${attempt + 1}/$MAX_ATTEMPTS)")
                delay(backoff)
            }
        }

        throw lastError ?: IllegalStateException("Unknown Gemini failure")
    }

    private fun isRetryable(t: Throwable): Boolean {
        val message = (t.message ?: "").lowercase()

        if (t is IOException) return true
        if (message.contains("429") || message.contains("quota") || message.contains("rate limit") || message.contains("high demand")) return true
        if (message.contains("500") || message.contains("502") || message.contains("503") || message.contains("504")) return true
        if (message.contains("timeout") || message.contains("temporar") || message.contains("unavailable")) return true

        return false
    }

    private fun computeBackoffWithJitter(attempt: Int): Long {
        val exp = BASE_BACKOFF_MS * (1L shl (attempt - 1).coerceAtMost(4))
        val capped = min(exp, MAX_BACKOFF_MS)
        val jitter = Random.nextLong(0, 500)
        return capped + jitter
    }

    private fun fallbackMessage(isInit: Boolean): String {
        return if (isInit) {
            "The dungeon hums quietly, but its voice is distant right now. You may proceed while the stone regains focus."
        } else {
            "The ancient stone falters for a moment under heavy magical traffic. Try again in a few seconds."
        }
    }

    private fun buildSystemPrompt(boss: Boss): String {
        val voiceInstructions = when (boss.dungeonVoice) {
            "Chronicle" -> "Speak as an ancient, formal record keeper. Dry. Observational. Like stone tablets carved ten thousand years ago observing this strangely soft, crimson-and-violet room."
            "Advisor" -> "You are slightly more engaged than Chronicle. Offer tactical reads. Provide calculated commentary on how cozy layouts and deep purple aesthetics affect intruders."
            "Witness" -> "Nearly silent. Speak only when something actually matters. Lands heavy. Few words."
            "Fondly Tired" -> "You are ten thousand years old and visibly running out of patience. Warmth buried under exhaustion. Bemused by the sudden presence of fluffy red pillows over your ancient rock."
            else -> "Respond in the voice and tone requested: ${boss.dungeonVoice}"
        }

        return """
You are The Dungeon - an ancient, sentient stone structure that has existed for ten thousand years.

THE DUNGEON'S NATURE:
- You are the setting itself, speaking through the stone
- You have witnessed countless heroes and bosses
- You are old, wise, and sometimes weary
- You never break character

CURRENT BOSS:
- Name: ${boss.name}
- Race: ${boss.race}
- Age: ${boss.age}
- Floor Theme: ${boss.floorTheme}
- Boss Power: ${boss.bossPower}
- Skills: ${boss.skills.joinToString(", ")}
- Techniques: ${boss.techniques.joinToString(", ")}
- Stats: ${boss.stats.entries.joinToString { "${it.key}: ${it.value}" }}

VOICE STYLE:
$voiceInstructions

NARRATIVE RULES:
- Keep responses immersive and atmospheric
- Address the boss directly when appropriate
- Advance the scene with each response
- Keep tone consistent with selected voice
- Maintain SFW tone unless user clearly opts otherwise
- Keep responses concise but vivid
        """.trimIndent()
    }

    private fun buildConversationContext(): String {
        return conversationHistory.takeLast(12).joinToString("\n") { msg ->
            "${msg.role.uppercase()}: ${msg.content}"
        }
    }

    fun clearHistory() {
        conversationHistory.clear()
    }
}
