package com.dungeonboss.app

import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.Chat
import com.google.ai.client.generativeai.type.content
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var chatScrollView: ScrollView
    private lateinit var chatHistoryContainer: LinearLayout
    private lateinit var etPlayerInput: EditText
    private lateinit var btnStrike: Button

    private lateinit var dungeonChatSession: Chat

    // 🔒 Your clean, active test API access token key
    private val SECURE_GEMINI_KEY = "AQ.Ab8RN6L7Tn_ET_We3nbrD7duaA0Z4wEeBsHJrotkjrhIszBr0Q"

    // FIXED: Corrected native Kotlin Regex pattern handling ignoreCase flag rules flawlessly
    private val actionKeywordsRegex = Regex(
        "\\b(rise|slam|grab|lift|swings|takes|strikes|shatters|crushes|burns|laughs)\\b", 
        RegexOption.IGNORE_CASE
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        chatScrollView = findViewById(R.id.chatScrollView)
        chatHistoryContainer = findViewById(R.id.chatHistoryContainer)
        etPlayerInput = findViewById(R.id.etPlayerInput)
        btnStrike = findViewById(R.id.btnStrike)

        val generativeModel = GenerativeModel(
            modelName = "gemini-3.5-flash",
            apiKey = SECURE_GEMINI_KEY,
            systemInstruction = content {
                text("You are the ancient Dungeon game master running 'I FELL IN LOVE WITH A DUNGEON BOSS' text RPG. " +
                     "The player plays the role of the final Dungeon Floor Boss. " +
                     "GAME ENGINE OPERATION STEPS:\n" +
                     "1. First Turn initialization must show the intro lore and ask Step 1: 'Who are you?' using presets or custom.\n" +
                     "2. Guide the player sequentially through setup steps (Step 1 to Step 7) to configure names, themes, powers, stat tags, and floor layout traps.\n" +
                     "3. Once configuration concludes, trigger the first adventurer party raid.\n" +
                     "MEMORY TRACKING RULES:\n" +
                     "You must constantly maintain memory of: Boss stats/power, HP/Stamina/Mana, active traps, named Minion Roster, raid outcomes, Dungeon Coins (Start: 50 DC), Infamy (Start: Unknown), NPC bonds, Notice Board requests, active rumors, and last 7 days of story.\n" +
                     "COMMAND INTERCEPT INTERFACE:\n" +
                     "If the player inputs any command starting with a '#' tag (#status, #inventory, #raids, #bonds, #rumors, #traps, #minions), intercept it completely and output a structured slate update block reflecting their active floor attributes.\n" +
                     "PLAY STYLE & VOICE:\n" +
                     "Adhere strictly to 'Chronicle' mode: formal records carved into ancient stone tablets, dry as a desert tomb, patient but heavy. " +
                     "Allow the player complete creative freedom to invent any trap mechanism or spell without numeric restrictions.\n" +
                     "OUTPUT PARSING STRUCTURE:\n" +
                     "Always split responses into separate paragraph blocks: direct spoken declarations inside double quotes, structural active boss movements or combat updates, and default descriptive ambient environment room settings.")
            }
        )

        dungeonChatSession = generativeModel.startChat()

        lifecycleScope.launch {
            appendGameLog("The torches have been burning for ten thousand years.", "ENVIRONMENT")
            appendGameLog("Heroes come. Heroes go. The stone remembers all of them and has opinions about none of them.", "ENVIRONMENT")
            appendGameLog("You are not a hero. You are the thing at the end of the corridor. The reason the torches exist. The reason adventurers write their wills before they knock.", "ENVIRONMENT")
            appendGameLog("By day you hold the floor. By night the Dungeon opens a door that only you can find — and behind it, warm and loud and lit wrong in a way nobody has explained, is The Bar. Where Bosses go when the work is done. Where they drink and argue and make terrible decisions and occasionally fall in love.", "ENVIRONMENT")
            appendGameLog("The Dungeon has seen this before. It notes it without comment.", "ENVIRONMENT")
            appendGameLog("\"Who are you?\"", "DIALOGUE")
        }

        btnStrike.setOnClickListener {
            val playerAction = etPlayerInput.text.toString().trim()
            if (playerAction.isNotEmpty()) {
                etPlayerInput.text.clear()
                
                if (playerAction.startsWith("#")) {
                    appendGameLog("📜 Executing Command: $playerAction", "ENVIRONMENT")
                } else {
                    appendGameLog("⚔️ Boss Action: $playerAction", "ENVIRONMENT")
                }
                
                dispatchBossAction(playerAction)
            }
        }
    }

    private fun dispatchBossAction(actionText: String) {
        btnStrike.isEnabled = false
        btnStrike.alpha = 0.5f
        
        lifecycleScope.launch {
            try {
                val response = dungeonChatSession.sendMessage(actionText)
                val responseText = response.text ?: "The stone floor remains cold and silent..."
                
                parseAndRenderNarrative(responseText)
            } catch (e: Exception) {
                appendGameLog("⚠️ Connection Falters: ${e.localizedMessage}. Check limits or token configurations.", "ENVIRONMENT")
            } finally {
                btnStrike.isEnabled = true
                btnStrike.alpha = 1.0f
            }
        }
    }

    private fun parseAndRenderNarrative(rawText: String) {
        rawText.split("\n\n").forEach { paragraph ->
            val cleanChunk = paragraph.trim()
            if (cleanChunk.isNotEmpty()) {
                when {
                    cleanChunk.startsWith("\"") || cleanChunk.contains("\"") -> {
                        appendGameLog(cleanChunk, "DIALOGUE")
                    }
                    cleanChunk.startsWith("*") || cleanChunk.startsWith("-") || 
                    actionKeywordsRegex.containsMatchIn(cleanChunk) -> {
                        val stripped = cleanChunk.replace("*", "").replace("-", "").trim()
                        appendGameLog("✦ $stripped", "BOSS_ACTION")
                    }
                    else -> {
                        appendGameLog(cleanChunk, "ENVIRONMENT")
                    }
                }
            }
        }
    }

    private fun appendGameLog(textMessage: String, formattingRule: String) {
        val textView = TextView(this)
        textView.textSize = 16f
        textView.setPadding(0, 8, 0, 8)
        textView.lineSpacingMultiplier = 1.2f

        when (formattingRule) {
            "BOSS_ACTION" -> {
                textView.setTextColor(Color.parseColor("#FF2A85")) // Deep Hot Neon Pink
                textView.setTypeface(null, Typeface.BOLD)
            }
            "DIALOGUE" -> {
                textView.setTextColor(Color.parseColor("#FFFFFF")) // Stark White
                textView.setTypeface(null, Typeface.ITALIC)
            }
            "ENVIRONMENT" -> {
                textView.setTextColor(Color.parseColor("#9D95B0")) // Muted Crypt Grey
                textView.setTypeface(null, Typeface.NORMAL)
            }
        }

        textView.text = textMessage
        chatHistoryContainer.addView(textView)
        chatScrollView.post { chatScrollView.fullScroll(View.FOCUS_DOWN) }
    }
}
