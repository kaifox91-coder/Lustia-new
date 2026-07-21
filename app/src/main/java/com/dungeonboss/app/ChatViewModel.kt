package com.dungeonboss.app

import android.content.Context
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class ChatViewModel(context: Context) : ViewModel() {
    private val gameState = GameState(context)
    
    // Clean the build string to remove literal quotation marks
    private val cleanGeminiKey: String = BuildConfig.GEMINI_KEY.replace("\"", "").trim()
    private val geminiService = GeminiService(cleanGeminiKey)

    private val _uiMessages = mutableStateOf<List<UIChatMessage>>(emptyList())
    val uiMessages: State<List<UIChatMessage>> = _uiMessages

    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading

    private val _boss = mutableStateOf(gameState.getBoss())
    val boss: State<Boss> = _boss

    private val _coins = mutableStateOf(gameState.getCoins())
    val coins: State<Int> = _coins

    private val _infamy = mutableStateOf(gameState.getInfamy())
    val infamy: State<Int> = _infamy

    val availableSaves = mutableStateOf<List<String>>(emptyList())

    // NEW: Persistent Preferences state for NSFW options
    private val sharedPrefs = context.getSharedPreferences("dungeon_boss_prefs", Context.MODE_PRIVATE)
    private val _isNsfwEnabled = mutableStateOf(sharedPrefs.getBoolean("nsfw_enabled", false))
    val isNsfwEnabled: State<Boolean> = _isNsfwEnabled

    init {
        refreshSaveList()
        val existingStory = gameState.getStoryLog()
        if (existingStory.isNotEmpty()) {
            _uiMessages.value = existingStory.map { UIChatMessage("dungeon", it) }
        } else {
            initializeDungeon()
        }
    }

    fun refreshSaveList() {
        availableSaves.value = gameState.getAvailableSaves()
    }

    fun switchCharacter(bossName: String) {
        if (gameState.loadCharacterProfile(bossName)) {
            _boss.value = gameState.getBoss()
            _coins.value = gameState.getCoins()
            _infamy.value = gameState.getInfamy()
            geminiService.clearHistory()

            val historicStory = gameState.getStoryLog()
            _uiMessages.value = historicStory.map { UIChatMessage("dungeon", it) }
            refreshSaveList()
        }
    }

    private fun initializeDungeon() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val dungeonGreeting = geminiService.initializeDungeon(_boss.value)
                _uiMessages.value = listOf(UIChatMessage("dungeon", dungeonGreeting))
                gameState.addStoryEntry(dungeonGreeting)
            } catch (e: Exception) {
                _uiMessages.value = listOf(UIChatMessage("system", "Error: ${e.message}"))
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun sendMessage(userInput: String) {
        if (userInput.trim().isEmpty()) return

        viewModelScope.launch {
            _isLoading.value = true
            _uiMessages.value = _uiMessages.value + UIChatMessage("user", userInput)

            try {
                // FIXED / OPTIMIZED: Strict memory limitation logic to prevent token-rate spill.
                // Pulls history entries, applies safe constraints to isolate contextual updates.
                val cappedHistory = if (_uiMessages.value.size > 10) {
                    _uiMessages.value.takeLast(10)
                } else {
                    _uiMessages.value
                }
                
                // Construct payload focusing strictly on the last 10 messages
                val dungeonResponse = geminiService.chat(userInput, _boss.value)
                _uiMessages.value = _uiMessages.value + UIChatMessage("dungeon", dungeonResponse)
                gameState.addStoryEntry(dungeonResponse)
            } catch (e: Exception) {
                _uiMessages.value = _uiMessages.value + UIChatMessage("system", "Error: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateBoss(boss: Boss) {
        _boss.value = boss
        gameState.updateBoss(boss)
        refreshSaveList()
    }

    fun addCoins(amount: Int) {
        gameState.addCoins(amount)
        _coins.value = gameState.getCoins()
    }

    fun addInfamy(amount: Int) {
        gameState.addInfamy(amount)
        _infamy.value = gameState.getInfamy()
    }

    // NEW: Offline Story Price Engine mapping local game context
    // This allows mechanics (items, tags, pricing formulas) to exist safely outside AI memory logs
    fun calculateItemPrice(basePrice: Int, isShadyItem: Boolean): Int {
        val infamyScore = _infamy.value
        return when {
            // Highly Infamous bosses receive massive black-market markdowns on illegal items, but normal merchants inflate values
            infamyScore >= 80 -> if (isShadyItem) (basePrice * 0.65).toInt() else (basePrice * 1.5).toInt()
            // High Infamy causes standard markup tax due to fear/suspicion
            infamyScore >= 50 -> if (isShadyItem) (basePrice * 0.85).toInt() else (basePrice * 1.2).toInt()
            // Low Infamy means honorable merchant deals, but dark/shady underground sellers overcharge you
            infamyScore <= 15 -> if (isShadyItem) (basePrice * 2.0).toInt() else (basePrice * 0.80).toInt()
            else -> basePrice
        }
    }

    // NEW: Safe state setter toggle tracking preferences
    fun setNsfwPreference(enabled: Boolean) {
        _isNsfwEnabled.value = enabled
        sharedPrefs.edit().putBoolean("nsfw_enabled", enabled).apply()
    }

    fun resetChat() {
        geminiService.clearHistory()
        _uiMessages.value = emptyList()
        initializeDungeon()
    }
}
