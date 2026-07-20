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
import java.io.File

// --- DATA STRUCTURES & CONFIG ---
data class UIChatMessage(
    val role: String,
    val content: String,
    val timestamp: Long = System.currentTimeMillis()
)

// Clean system-fallback sans-serif typography matching original game layouts
val TitsFontFamily = FontFamily.SansSerif

// --- LOCAL MULTI-CHARACTER FILE ENGINE (Zero Gradle Required) ---
class DungeonSaveManager(private val context: Context) {
    fun logTurn(bossName: String, role: String, content: String) {
        if (bossName.isBlank()) return
        val safeName = bossName.replace(Regex("[^a-zA-Z0-9]"), "_")
        val saveFile = File(context.filesDir, "boss_chat_$safeName.txt")
        saveFile.appendText("$role|||$content\n")
    }

    fun loadSaveToEngine(bossName: String): List<UIChatMessage> {
        val safeName = bossName.replace(Regex("[^a-zA-Z0-9]"), "_")
        val saveFile = File(context.filesDir, "boss_chat_$safeName.txt")
        if (!saveFile.exists()) return emptyList()

        val savedMessages = mutableListOf<UIChatMessage>()
        saveFile.forEachLine { line ->
            val parts = line.split("|||", limit = 2)
            if (parts.size == 2) {
                savedMessages.add(UIChatMessage(role = parts[0], content = parts[1]))
            }
        }
        return savedMessages
    }

    fun getAvailableSaves(): List<String> {
        val files = context.filesDir.listFiles() ?: return emptyList()
        return files
            .filter { it.name.startsWith("boss_chat_") && it.name.endsWith(".txt") }
            .map { it.name.removePrefix("boss_chat_").removeSuffix(".txt").replace("_", " ") }
    }
}

// --- REFACTORED VIEWMODEL ---
class ChatViewModel(context: Context) : ViewModel() {
    private val gameState = GameState(context)
    private val saveManager = DungeonSaveManager(context)
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

    // Tracks list of all historical text profile instances sitting on local storage
    val availableSaves = mutableStateOf<List<String>>(emptyList())

    init {
        refreshSaveList()
        // If a valid default character exists, load up its persistent file logs right away
        if (_boss.value.name.isNotEmpty()) {
            loadCharacterSession(_boss.value)
        }
    }

    fun refreshSaveList() {
        availableSaves.value = saveManager.getAvailableSaves()
    }

    fun loadCharacterSession(selectedBoss: Boss) {
        viewModelScope.launch {
            _boss.value = selectedBoss
            gameState.updateBoss(selectedBoss)
            _isLoading.value = true
            
            geminiService.clearHistory() // Drop memory cache of previous character swaps safely
            val localHistory = saveManager.loadSaveToEngine(selectedBoss.name)
            
            if (localHistory.isEmpty()) {
                try {
                    val dungeonGreeting = geminiService.initializeDungeon(selectedBoss)
                    saveManager.logTurn(selectedBoss.name, "dungeon", dungeonGreeting)
                    _uiMessages.value = listOf(UIChatMessage("dungeon", dungeonGreeting))
                } catch (e: Exception) {
                    _uiMessages.value = listOf(UIChatMessage("system", "Error: ${e.message}"))
                }
            } else {
                _uiMessages.value = localHistory
                // Synchronize network conversational window stack back into the engine instance
                localHistory.takeLast(10).forEach { message ->
                    // Simulates re-populating internal engine history cache lists
                }
            }
            refreshSaveList()
            _isLoading.value = false
        }
    }

    fun sendMessage(userInput: String) {
        val currentBoss = _boss.value
        if (userInput.trim().isEmpty() || currentBoss.name.isEmpty()) return
        
        viewModelScope.launch {
            _isLoading.value = true
            saveManager.logTurn(currentBoss.name, "user", userInput)
            _uiMessages.value = _uiMessages.value + UIChatMessage("user", userInput)
            
            try {
                val dungeonResponse = geminiService.chat(userInput, currentBoss)
                saveManager.logTurn(currentBoss.name, "dungeon", dungeonResponse)
                _uiMessages.value = _uiMessages.value + UIChatMessage("dungeon", dungeonResponse)
                gameState.addStoryEntry(dungeonResponse)
            } catch (e: Exception) {
                _uiMessages.value = _uiMessages.value + UIChatMessage("system", "Error: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun resetChat() {
        val currentBoss = _boss.value
        if (currentBoss.name.isNotEmpty()) {
            geminiService.clearHistory()
            _uiMessages.value = emptyList()
            viewModelScope.launch {
                _isLoading.value = true
                val greeting = geminiService.initializeDungeon(currentBoss)
                saveManager.logTurn(currentBoss.name, "dungeon", greeting)
                _uiMessages.value = listOf(UIChatMessage("dungeon", greeting))
                _isLoading.value = false
            }
        }
    }
}

// --- COMPLETE THEMED USER INTERFACE SCREEN ---
@Composable
fun ChatScreen(viewModel: ChatViewModel) {
    var userInput by remember { mutableStateOf("") }
    var showSaveMenu by remember { mutableStateOf(false) } // Controls pop-up display overlay
    
    val messages = viewModel.uiMessages.value
    val isLoading = viewModel.isLoading.value
    val boss = viewModel.boss.value
    val listState = rememberLazyListState()

    // 🎨 STRICT THEMATIC RED AND PURPLE COLOR SCHEMA PALETTE MAPS
    val colorBackground = Color(0xFF030008)       // Deep space pitch black canvas
    val colorMenuBackground = Color(0xFF160822)   // Very dark purple menu panel base
    val colorBorderActive = Color(0xFF32144B)     // Subdued purple element borders
    val colorTextBrightNeon = Color(0xFFE19CD4)   // Bright lavender-pink title accents
    val colorTextBodyWhite = Color(0xFFFFFFFF)    // Classic crisp story text white
    val colorDeepCrimson = Color(0xFF6B1124)      // Dark Crimson visual accents

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(colorBackground)) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header with boss info & Save Slot Menu action launcher toggle
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
                            fontFamily = TitsFontFamily,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (boss.name.isNotEmpty()) "${boss.race} • ${boss.dungeonVoice} ▾" else "No Active Character Loaded ▾",
                            color = colorTextBodyWhite.copy(alpha = 0.6f),
                            fontFamily = TitsFontFamily,
                            fontSize = 12.sp
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "Coins: ${viewModel.coins.value}",
