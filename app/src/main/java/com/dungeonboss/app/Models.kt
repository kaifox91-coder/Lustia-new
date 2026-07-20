package com.dungeonboss.app

import android.content.Context
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class ChatViewModel(context: Context) : ViewModel() {
    private val gameState = GameState(context)
    private val geminiService = GeminiService(BuildConfig.GEMINI_API_KEY.trim())

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

    fun resetChat() {
        geminiService.clearHistory()
        _uiMessages.value = emptyList()
        initializeDungeon()
    }
}
