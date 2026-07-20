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
    private val geminiService = GeminiService(BuildConfig.GEMINI_API_KEY)

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
    var showDebugPanel by remember { mutableStateOf(false) }

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
                        if (BuildConfig.DEBUG) {
                            TextButton(
                                onClick = { showDebugPanel = true },
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                            ) {
                                Text(
                                    text = "Debug",
                                    color = colorTextBrightNeon,
                                    fontFamily = FontFamily.SansSerif,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
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
                            )
                        }
                    }
                }
            }

            // Input area
            Surface(
                modifier = Modifier.fillMaxWidth().padding(8.dp),
                color = colorMenuBackground,
                shape = RoundedCornerShape(4.dp),
                border = BorderStroke(1.dp, colorBorderActive)
            ) {
                Row(
                    modifier = Modifier.padding(8.dp).fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextField(
                        value = userInput,
                        onValueChange = { userInput = it },
                        modifier = Modifier.weight(1f).height(50.dp),
                        placeholder = {
                            Text(
                                text = "Speak to the Dungeon...",
                                color = colorTextBodyWhite.copy(alpha = 0.3f),
                                fontFamily = FontFamily.SansSerif,
                                fontSize = 13.sp
                            )
                        },
                        textStyle = TextStyle(fontFamily = FontFamily.SansSerif, fontSize = 14.sp),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = colorBackground,
                            unfocusedContainerColor = colorBackground,
                            focusedTextColor = colorTextBodyWhite,
                            unfocusedTextColor = colorTextBodyWhite,
                            focusedIndicatorColor = colorTextBrightNeon,
                            unfocusedIndicatorColor = colorBorderActive
                        ),
                        shape = RoundedCornerShape(4.dp),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = {
                            viewModel.sendMessage(userInput)
                            userInput = ""
                        },
                        enabled = !isLoading && userInput.trim().isNotEmpty() && boss.name.isNotEmpty(),
                        modifier = Modifier.height(50.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colorDeepCrimson,
                            contentColor = colorTextBodyWhite,
                            disabledContainerColor = colorBackground
                        ),
                        shape = RoundedCornerShape(4.dp),
                        border = BorderStroke(1.dp, if (userInput.trim().isNotEmpty()) colorTextBrightNeon else colorBorderActive)
                    ) {
                        Text(
                            text = "Send",
                            fontFamily = FontFamily.SansSerif,
                            fontWeight = FontWeight.Bold,
                            color = if (userInput.trim().isNotEmpty()) colorTextBodyWhite else Color.Gray
                        )
                    }
                }
            }
        }

        // --- PURPLE THEMED MULTI-SAVE LOAD GAME POPUP ---
        if (showSaveMenu) {
            AlertDialog(
                onDismissRequest = { showSaveMenu = false },
                title = { Text("Load Saved Character File", color = colorTextBrightNeon, fontFamily = FontFamily.SansSerif) },
                text = {
                    val savesList = viewModel.availableSaves.value
                    if (savesList.isEmpty()) {
                        Text("No saved character files found on your device.", color = colorTextBodyWhite, fontFamily = FontFamily.SansSerif)
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth().heightIn(max = 250.dp)
                        ) {
                            items(savesList) { saveName ->
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            viewModel.switchCharacter(saveName)
                                            showSaveMenu = false
                                        },
                                    color = colorBackground,
                                    border = BorderStroke(1.dp, colorBorderActive),
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = saveName,
                                        modifier = Modifier.padding(12.dp),
                                        color = colorTextBodyWhite,
                                        fontFamily = FontFamily.SansSerif,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showSaveMenu = false }) {
                        Text("Close", color = colorTextBrightNeon, fontFamily = FontFamily.SansSerif)
                    }
                },
                containerColor = colorMenuBackground,
                shape = RoundedCornerShape(4.dp),
                modifier = Modifier.padding(16.dp)
            )
        }

        if (BuildConfig.DEBUG && showDebugPanel) {
            DebugPanelDialog(
                bossName = boss.name,
                messageCount = messages.size,
                apiKeyPresent = BuildConfig.GEMINI_API_KEY.isNotBlank(),
                packageName = BuildConfig.APPLICATION_ID,
                onDismiss = { showDebugPanel = false },
                colorMenuBackground = colorMenuBackground,
                colorTextBrightNeon = colorTextBrightNeon,
                colorTextBodyWhite = colorTextBodyWhite,
                colorBorderActive = colorBorderActive
            )
        }
    }
}

@Composable
private fun DebugPanelDialog(
    bossName: String,
    messageCount: Int,
    apiKeyPresent: Boolean,
    packageName: String,
    onDismiss: () -> Unit,
    colorMenuBackground: Color,
    colorTextBrightNeon: Color,
    colorTextBodyWhite: Color,
    colorBorderActive: Color
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Debug Panel",
                color = colorTextBrightNeon,
                fontFamily = FontFamily.SansSerif,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                DebugRow(label = "Build Type", value = if (BuildConfig.DEBUG) "Debug" else "Release", colorTextBodyWhite)
                DebugRow(label = "Package", value = packageName, colorTextBodyWhite)
                DebugRow(label = "Boss", value = bossName.ifBlank { "(none)" }, colorTextBodyWhite)
                DebugRow(label = "Messages", value = messageCount.toString(), colorTextBodyWhite)
                DebugRow(label = "Gemini API Key", value = if (apiKeyPresent) "Present" else "Missing", colorTextBodyWhite)
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = colorTextBrightNeon, fontFamily = FontFamily.SansSerif)
            }
        },
        containerColor = colorMenuBackground,
        shape = RoundedCornerShape(4.dp),
        modifier = Modifier.padding(16.dp)
    )
}

@Composable
private fun DebugRow(label: String, value: String, valueColor: Color) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(
            text = "$label:",
            color = valueColor.copy(alpha = 0.7f),
            fontFamily = FontFamily.SansSerif,
            fontSize = 13.sp
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = value,
            color = valueColor,
            fontFamily = FontFamily.SansSerif,
            fontWeight = FontWeight.SemiBold,
            fontSize = 13.sp
        )
    }
}

@Composable
fun ChatMessageBubble(
    message: UIChatMessage,
    fontFamily: FontFamily,
    bodyColor: Color,
    systemColor: Color,
    panelColor: Color,
    borderColor: Color
) {
    val isUserMessage = message.role == "user"

    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
        horizontalArrangement = if (isUserMessage) Arrangement.End else Arrangement.Start
    ) {
        Surface(
            modifier = Modifier.widthIn(max = 320.dp),
            color = if (isUserMessage) panelColor else Color.Transparent,
            shape = RoundedCornerShape(4.dp),
            border = if (isUserMessage) BorderStroke(1.dp, borderColor) else null
        ) {
            Text(
                text = message.content,
                modifier = Modifier.padding(if (isUserMessage) 12.dp else 4.dp),
                color = if (isUserMessage) bodyColor else systemColor,
                fontFamily = fontFamily,
                fontSize = 14.sp,
                lineHeight = 20.sp
            )
        }
    }
}
