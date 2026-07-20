package com.dungeonboss.app

import android.content.Context
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

data class UIChatMessage(
    val role: String,
    val content: String,
    val timestamp: Long = System.currentTimeMillis()
)

class ChatViewModel(context: Context) : ViewModel() {
    private val gameState = GameState(context)
    private val apiKey = "AQ.Ab8RN6KoWNDvJGLAkKCIYweGI9lWCh49o2we6VwpsJvqcbG4VA" 
    private val geminiService = GeminiService(apiKey)
    
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
        // Rebuild old UI logs using your game's persistent story log history array
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

    // 📱 SWAPS CHARACTERS AND RESTORES RUNTIME VARIABLES
    fun switchCharacter(bossName: String) {
        if (gameState.loadCharacterProfile(bossName)) {
            _boss.value = gameState.getBoss()
            _coins.value = gameState.getCoins()
            _infamy.value = gameState.getInfamy()
            geminiService.clearHistory() // Flush AI memory stack safely
            
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
                gameState.addStoryEntry(dungeonResponse) // Persists data straight into active save slot
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

@Composable
fun ChatScreen(viewModel: ChatViewModel) {
    var userInput by remember { mutableStateOf("") }
    var showSaveMenu by remember { mutableStateOf(false) } 
    
    val messages = viewModel.uiMessages.value
    val isLoading = viewModel.isLoading.value
    val boss = viewModel.boss.value
    val listState = rememberLazyListState()

    // 🎨 CENTRAL DESIGN GROUP CONTROLS (Purple and Red Vibe)
    val colorBackground = Color(0xFF030008)       
    val colorMenuBackground = Color(0xFF160822)   
    val colorBorderActive = Color(0xFF32144B)     
    val colorTextBrightNeon = Color(0xFFE19CD4)   
    val colorTextBodyWhite = Color(0xFFFFFFFF)    
    val colorDeepCrimson = Color(0xFF6B1124)      

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(colorBackground)) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header: Click character block to reveal Save Profile selector lists
            Surface(
                modifier = Modifier.fillMaxWidth().padding(8.dp),
                color = colorMenuBackground,
                shape = RoundedCornerShape(4.dp),
                border = BorderStroke(1.dp, colorBorderActive)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { viewModel.refreshSaveList(); showSaveMenu = true }
                    ) {
                        Text(
                            text = boss.name.ifEmpty { "Tap to Select Save Slot" },
                            color = colorTextBrightNeon,
                            fontFamily = FontFamily.SansSerif,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (boss.name.isNotEmpty()) "${boss.race} • ${boss.dungeonVoice} ▾" else "No Active Character Loaded ▾",
                            color = colorTextBodyWhite.copy(alpha = 0.6f),
                            fontFamily = FontFamily.SansSerif,
                            fontSize = 12.sp
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "Coins: ${viewModel.coins.value}",
                            color = colorTextBrightNeon,
                            fontFamily = FontFamily.SansSerif,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Infamy: ${viewModel.infamy.value}",
                            color = colorDeepCrimson,
                            fontFamily = FontFamily.SansSerif,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Chat messages
            LazyColumn(
                modifier = Modifier.weight(1f).fillMaxWidth().padding(8.dp),
                state = listState,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(messages) { message ->
                    ChatMessageBubble(
                        message = message,
                        fontFamily = FontFamily.SansSerif,
                        bodyColor = colorTextBodyWhite,
                        systemColor = colorTextBrightNeon,
                        panelColor = colorMenuBackground,
                        borderColor = colorBorderActive
                    )
                }
                
                if (isLoading) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(8.dp),
                            horizontalArrangement = Arrangement.Start
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = colorTextBrightNeon,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "The Dungeon builds textures...",
                                color = colorTextBodyWhite.copy(alpha = 0.5f),
                                fontFamily = FontFamily.SansSerif,
                                fontStyle = FontStyle.Italic,
                                fontSize = 13.sp
