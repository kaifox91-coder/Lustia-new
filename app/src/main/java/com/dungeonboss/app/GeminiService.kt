package com.dungeonboss.app

import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class ChatMessage(
    val role: String,
    val content: String
)

class GeminiService(apiKey: String) {
    private val model = GenerativeModel(
        modelName = "gemini-1.5-flash",
        apiKey = apiKey
    )

    private val conversationHistory = mutableListOf<ChatMessage>()

    suspend fun initializeDungeon(boss: Boss): String = withContext(Dispatchers.IO) {
        val systemPrompt = buildSystemPrompt(boss)
        conversationHistory.clear()
        
        val response = try {
            val content = content {
                text(systemPrompt + "\n\nGreet the boss and set the scene for the first time at The Bar.")
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
            "Chronicle" -> "Speak as an ancient, formal record keeper. Dry. Observational. Like stone tablets carved ten thousand years ago."
            "Advisor" -> "You are slightly more engaged than Chronicle. Offer tactical reads. Occasionally offer opinions about the situation."
            "Witness" -> "Nearly silent. Speak only when something actually matters. Lands heavy. Few words."
            "Fondly Tired" -> "You are ten thousand years old and visibly running out of patience. Warmth buried under exhaustion. Occasionally exasperated."
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
Floor Theme: ${boss.floorTheme}
Dungeon Voice Style: $voiceInstructions

SETTING:
Your dungeon exists in: ${boss.setting}

YOUR INSTRUCTIONS:
1. Respond as The Dungeon speaking through the stone
2. Your voice is: $voiceInstructions
3. Keep responses 2-4 paragraphs maximum
4. Never break character - you are the stone, not a game master
5. Describe atmosphere, consequences, and what the stone observes
6. Reference the boss's power, appearance, and abilities when relevant
7. Build narrative weight through description
8. When the boss takes action, describe what the dungeon feels/observes
9. Honor the gravity of their choices

TONE GUIDE:
- Ancient. Aware. Slightly amused or exasperated depending on voice mode
- The dungeon does not judge but it records
- Your words carry weight because you've seen ten thousand years of similar scenes
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
