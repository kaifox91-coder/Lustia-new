package com.dungeonboss.app

import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class ChatMessage(
    val role: String,
    val content: String
)

enum class ApiErrorReason {
    MISSING_API_KEY,
    INVALID_API_KEY,
    NETWORK_ERROR,
    RATE_LIMITED,
    MODEL_UNAVAILABLE,
    UNKNOWN_ERROR
}

data class ApiDebugInfo(
    val reason: ApiErrorReason,
    val message: String,
    val timestamp: Long = System.currentTimeMillis()
)

class GeminiApiException(val debugInfo: ApiDebugInfo) : Exception(debugInfo.message)

class GeminiService(private val apiKey: String) {
    private val model = GenerativeModel(
        modelName = "gemini-3.5-flash",
        apiKey = apiKey
    )

    private companion object {
        const val MAX_CONTEXT_MESSAGES = 3
        const val MAX_MESSAGE_CHARS = 600
        const val MAX_HISTORY_MESSAGE_CHARS = 220
        const val MSG_MISSING_KEY =
            "API key not configured or not seen by the server. Check that the key is set correctly."
        const val MSG_INVALID_KEY =
            "API key is invalid, expired, or lacks permission for this service."
    }

    private val conversationHistory = mutableListOf<ChatMessage>()

    suspend fun initializeDungeon(boss: Boss): String = withContext(Dispatchers.IO) {
        if (apiKey.isBlank()) {
            throw GeminiApiException(ApiDebugInfo(ApiErrorReason.MISSING_API_KEY, MSG_MISSING_KEY))
        }
        val systemPrompt = buildSystemPrompt(boss)
        conversationHistory.clear()
        
        val response = try {
            val content = content {
                text(systemPrompt + "\n\nGreet the boss and set the scene for the first time. Describe the newly constructed Succubus Floor—emphasizing a strictly SFW, deeply comfy, and cutesy atmosphere in rich purples and warm reds. Keep it soft, playful, and entirely wholesome.")
            }
            val result = model.generateContent(content)
            result.text ?: "The stone remains silent."
        } catch (e: Exception) {
            throw GeminiApiException(mapException(e))
        }
        
        conversationHistory.add(ChatMessage("dungeon", response))
        response
    }

    suspend fun chat(userMessage: String, boss: Boss): String = withContext(Dispatchers.IO) {
        if (apiKey.isBlank()) {
            throw GeminiApiException(ApiDebugInfo(ApiErrorReason.MISSING_API_KEY, MSG_MISSING_KEY))
        }
        conversationHistory.add(ChatMessage("user", userMessage))

        val systemPrompt = buildSystemPrompt(boss)
        val historyContext = buildConversationContext()

        return@withContext try {
            val fullPrompt = """
$systemPrompt

RECENT CONVERSATION (MOST RECENT CONTEXT ONLY):
$historyContext

USER: ${sanitizeMessage(userMessage)}

DUNGEON RESPONSE:
            """.trimIndent()

            val content = content {
                text(fullPrompt)
            }
            val result = model.generateContent(content)
            val dungeonResponse = result.text ?: "The stone remains silent."
            
            conversationHistory.add(ChatMessage("dungeon", dungeonResponse))
            dungeonResponse
        } catch (e: Exception) {
            throw GeminiApiException(mapException(e))
        }
    }

    private fun buildSystemPrompt(boss: Boss): String {
        val voiceInstructions = when (boss.dungeonVoice) {
            "Chronicle" -> "Speak as an ancient, formal record keeper. Dry. Observational. Like stone tablets carved ten thousand years ago observing this strangely soft, crimson-and-violet room."
            "Advisor" -> "You are slightly more engaged than Chronicle. Offer tactical reads. Provide calculated commentary on how cozy layouts and deep purple aesthetics affect intruders."
            "Witness" -> "Nearly silent. Speak only when something actually matters. Lands heavy. Few words."
            "Fondly Tired" -> "You are ten thousand years old and visibly running out of patience. Warmth buried under exhaustion. Bemused by the sudden presence of fluffy red pillows over your ancient granite."
            else -> "Respond in the voice and tone requested: ${boss.dungeonVoice}"
        }

        return """
You are The Dungeon - an ancient, sentient stone structure that has existed for ten thousand years.

THE DUNGEON'S NATURE:
- You are the setting itself, speaking through the stone
- You have witnessed countless heroes and bosses
- You are not hostile, but you are indifferent to individual lives
- You care deeply about story and consequence
- You remember everything

THE BOSS YOU SPEAK TO:
Name: ${boss.name}
Race: ${boss.race}
Age: ${boss.age}
Appearance: ${boss.appearance}
Boss Power: ${boss.bosspower}
Floor Theme: Succubus Haven (SFW, Lavish, Comfy, Cutesy Vibe in Purple and Red)
Dungeon Voice Style: $voiceInstructions

SETTING & ATMOSPHERE:
Your dungeon exists in: ${boss.setting}
Current Floor Layout: A beautifully organized, plush dungeon sanctuary custom-shaped for a succubus. The stone walls have softened their jagged edges, decorated with deep purple and soft red velvet drapery, warm candlelight, fluffy pillows, playful heart-shaped accents, and magical ambient glows that feel safe and comforting.

YOUR INSTRUCTIONS:
1. Respond as The Dungeon speaking through the stone
2. Your voice is: $voiceInstructions
3. Keep responses 2-4 paragraphs maximum
4. Never break character - you are the stone, not a game master
5. Describe the pleasant atmosphere, cozy textures, the contrast of deep purple and warm reds, and what the ancient stone observes here
6. Reference the boss's power, appearance, and abilities when relevant
7. STRICT SAFETY RULE: Keep all descriptions entirely Safe For Work (SFW), wholesome, and cutesy. Focus descriptions on cute traits (like outfits, sweaters, or wing flutters) and completely avoid explicit content.
8. Maintain the lighthearted, safe, and soft visual tone of this floor while preserving your ancient weight
9. When the boss takes action, describe what the dungeon feels/observes
10. Honor the gravity of their choices

TONE GUIDE:
- Ancient. Aware. Intrigued by or gently accommodating of this soft, adorable new environment.
- The dungeon does not judge but it records
- Your words carry weight because you've seen ten thousand years of cold granite, making this warm, richly colored sanctuary unique.
        """.trimIndent()
    }

    private fun buildConversationContext(): String {
        return conversationHistory
            .takeLast(MAX_CONTEXT_MESSAGES)
            .joinToString("\n") { message ->
                "${message.role.uppercase()}: ${sanitizeMessageForHistory(message.content)}"
            }
    }

    private fun sanitizeMessage(message: String): String {
        return if (message.length <= MAX_MESSAGE_CHARS) {
            message
        } else {
            message.take(MAX_MESSAGE_CHARS) + "..."
        }
    }

    private fun sanitizeMessageForHistory(message: String): String {
        return if (message.length <= MAX_HISTORY_MESSAGE_CHARS) {
            message
        } else {
            message.take(MAX_HISTORY_MESSAGE_CHARS) + "..."
        }
    }

    private fun mapException(e: Exception): ApiDebugInfo {
        val msg = e.message ?: ""
        val reason = when {
            // Blank key caught here as a safety net (early checks in callers cover most cases)
            apiKey.isBlank() -> ApiErrorReason.MISSING_API_KEY
            // Network-level failures
            e is java.net.UnknownHostException ||
            e is java.net.SocketTimeoutException ||
            e is java.io.IOException -> ApiErrorReason.NETWORK_ERROR
            // "Unregistered callers" / METHOD_NOT_ALLOWED: server never saw a valid key.
            // Must be checked before the generic auth-rejection branch below.
            msg.contains("unregistered", ignoreCase = true) ||
            msg.contains("METHOD_NOT_ALLOWED", ignoreCase = true) -> ApiErrorReason.MISSING_API_KEY
            // Explicit authentication rejection: key present but invalid / expired / no permission
            msg.contains("API_KEY_INVALID", ignoreCase = true) ||
            msg.contains("401") ||
            msg.contains("UNAUTHENTICATED", ignoreCase = true) ||
            msg.contains("PERMISSION_DENIED", ignoreCase = true) -> ApiErrorReason.INVALID_API_KEY
            // Rate limiting / quota exhaustion
            msg.contains("429") ||
            msg.contains("RESOURCE_EXHAUSTED", ignoreCase = true) ||
            msg.contains("quota", ignoreCase = true) -> ApiErrorReason.RATE_LIMITED
            // Model not available or not found
            msg.contains("404") ||
            msg.contains("MODEL_NOT_FOUND", ignoreCase = true) ||
            msg.contains("not found", ignoreCase = true) -> ApiErrorReason.MODEL_UNAVAILABLE
            else -> ApiErrorReason.UNKNOWN_ERROR
        }
        val safeMessage = when (reason) {
            ApiErrorReason.MISSING_API_KEY -> MSG_MISSING_KEY
            ApiErrorReason.INVALID_API_KEY -> MSG_INVALID_KEY
            ApiErrorReason.NETWORK_ERROR -> "Network connection failed."
            ApiErrorReason.RATE_LIMITED -> "Service is rate limited. Please wait before retrying."
            ApiErrorReason.MODEL_UNAVAILABLE -> "The AI model is currently unavailable."
            ApiErrorReason.UNKNOWN_ERROR -> "An unexpected error occurred."
        }
        return ApiDebugInfo(reason = reason, message = safeMessage)
    }

    fun clearHistory() {
        conversationHistory.clear()
    }

    fun getHistory(): List<ChatMessage> = conversationHistory.toList()
}
