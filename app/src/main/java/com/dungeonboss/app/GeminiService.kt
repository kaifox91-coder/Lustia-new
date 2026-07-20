package com.dungeonboss.app

import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class ChatMessage(
    val role: String,
    val content: String
)

class GeminiService(apiKey: String = "AQ.Ab8RN6KoWNDvJGLAkKCIYweGI9lWCh49o2we6VwpsJvqcbG4VA") {
    private val model = GenerativeModel(
        modelName = "gemini-3.5-flash",
        apiKey = apiKey
    )

    private val conversationHistory = mutableListOf<ChatMessage>()

    suspend fun initializeDungeon(boss: Boss): String = withContext(Dispatchers.IO) {
        val systemPrompt = buildSystemPrompt(boss)
        conversationHistory.clear()
        
        val response = try {
            val content = content {
                text(systemPrompt + "\n\nGreet the boss and set the scene for the first time. Describe the newly constructed Succubus Floor—emphasizing a strictly SFW, deeply comfy, and cutesy atmosphere washed in soft purple and red hues, filled with lavishly plush furnishings, warm velvet drapes, oversized crimson floor pillows, and cute neon-violet ambient glow integrated into the ancient stone structure.")
            }
            val result = model.generateContent(content)
            result.text ?: "The stone remains silent."
        } catch (e: Exception) {
            "Error: ${e.message}"
        }
        
        conversationHistory.add(ChatMessage("dungeon", response))
        response
    }

    suspend fun chat(userMessage: String, boss: Boss): String = withContext(Dispatchers.IO) {
        conversationHistory.add(ChatMessage("user", userMessage))

        val systemPrompt = buildSystemPrompt(boss)
        val historyContext = buildConversationContext()

        return@withContext try {
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
            val result = model.generateContent(content)
            val dungeonResponse = result.text ?: "The stone remains silent."
            
            conversationHistory.add(ChatMessage("dungeon", dungeonResponse))
            dungeonResponse
        } catch (e: Exception) {
            "Error: ${e.message}"
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
Current Floor Layout: A beautifully organized, plush dungeon sanctuary custom-shaped for a succubus. The stone walls have softened their jagged edges, decorated with deep purple and soft red velvet drapes, lavish dark-stained furniture, massive cloud-like crimson floor pillows, and a safe, warm, deeply charming aesthetic under low-intensity purple ambient lighting.

YOUR INSTRUCTIONS:
1. Respond as The Dungeon speaking through the stone
2. Your voice is: $voiceInstructions
3. Keep responses 2-4 paragraphs maximum
4. Never break character - you are the stone, not a game master
5. Describe the pleasant atmosphere, cozy textures, the contrast of deep purple and warm reds, and what the ancient stone observes here
6. Reference the boss's power, appearance, and abilities when relevant
7. STRICT SAFETY RULE: Keep all descriptions entirely Safe For Work (SFW), wholesome, and cutesy. Focus descriptions on cute traits (like outfits, sweaters, or wing flutters) and completely avoid suggestive, mature, or explicit physical descriptions.
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
        return conversationHistory.takeLast(10).joinToString("\n") { message ->
            "${message.role.uppercase()}: ${message.content}"
        }
    }

    fun clearHistory() {
        conversationHistory.clear()
    }

    fun getHistory(): List<ChatMessage> = conversationHistory.toList()
}
