package com.dungeonboss.app

import android.content.Context
import androidx.compose.foundation.background
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
    private val apiKey = "AQ.Ab8RN6KoWNDvJGLAkKCIYweGI9lWCh49o2we6VwpsJvqcbG4VA" // Replace with actual key from BuildConfig or secure storage
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

    init {
        initializeDungeon()
    }

    private fun initializeDungeon() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val dungeonGreeting = geminiService.initializeDungeon(_boss.value)
                _uiMessages.value = listOf(
                    UIChatMessage("dungeon", dungeonGreeting)
                )
            } catch (e: Exception) {
                _uiMessages.value = listOf(
                    UIChatMessage("system", "Error: ${e.message}")
                )
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun sendMessage(userInput: String) {
        if (userInput.trim().isEmpty()) return
        
        viewModelScope.launch {
            _isLoading.value = true
            
            // Add user message to UI
            _uiMessages.value = _uiMessages.value + UIChatMessage("user", userInput)
            
            try {
                val dungeonResponse = geminiService.chat(userInput, _boss.value)
                _uiMessages.value = _uiMessages.value + UIChatMessage("dungeon", dungeonResponse)
                
                // Save story entry
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
    val messages = viewModel.uiMessages.value
    val isLoading = viewModel.isLoading.value
    val boss = viewModel.boss.value
    val listState = rememberLazyListState()

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1A1A2E))
    ) {
        // Header with boss info
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            color = Color(0xFF16213E),
            shape = RoundedCornerShape(8.dp)
        ) {
            Row(
                modifier = Modifier
                    .padding(12.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = boss.name.ifEmpty { "Unnamed Boss" },
                        color = Color(0xFFD4AF37),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${boss.race} • ${boss.dungeonVoice}",
                        color = Color(0xFF888888),
                        fontSize = 12.sp
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Coins: ${viewModel.coins.value}",
                        color = Color(0xFFD4AF37),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Infamy: ${viewModel.infamy.value}",
                        color = Color(0xFFFF6B6B),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Chat messages
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(8.dp),
            state = listState,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(messages) { message ->
                ChatMessageBubble(message)
            }
            
            if (isLoading) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.Start
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Color(0xFFD4AF37),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "The Dungeon is thinking...",
                            color = Color(0xFF888888),
                            fontStyle = FontStyle.Italic
                        )
                    }
                }
            }
        }

        // Input area
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            color = Color(0xFF16213E),
            shape = RoundedCornerShape(8.dp)
        ) {
            Row(
                modifier = Modifier
                    .padding(8.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextField(
                    value = userInput,
                    onValueChange = { userInput = it },
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp),
                    placeholder = {
                        Text(
                            "Speak to the Dungeon...",
                            color = Color(0xFF666666),
                            fontSize = 12.sp
                        )
                    },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFF0F3460),
                        unfocusedContainerColor = Color(0xFF0F3460),
                        focusedTextColor = Color(0xFFFFFFFF),
                        unfocusedTextColor = Color(0xFFFFFFFF),
                        focusedIndicatorColor = Color(0xFFD4AF37),
                        unfocusedIndicatorColor = Color(0xFF333333)
                    ),
                    shape = RoundedCornerShape(8.dp),
                    singleLine = true
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Button(
                    onClick = {
                        viewModel.sendMessage(userInput)
                        userInput = ""
                    },
                    enabled = !isLoading && userInput.trim().isNotEmpty(),
                    modifier = Modifier.height(50.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFD4AF37),
                        disabledContainerColor = Color(0xFF666666)
                    )
                ) {
                    Text("Send", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun ChatScreen(viewModel: ChatViewModel) {
    var userInput by remember { mutableStateOf("") }
    val messages = viewModel.uiMessages.value
    val isLoading = viewModel.isLoading.value
    val boss = viewModel.boss.value
    val listState = rememberLazyListState()

    // 🎨 EXACT COMPONENT COLOR MAPS FROM THE SCREENSHOT
    val colorBackground = Color(0xFF030008)       // Deep space pitch black canvas
    val colorMenuBackground = Color(0xFF160822)   // Very dark purple menu panel base
    val colorBorderActive = Color(0xFF32144B)     // Subdued purple element borders
    val colorTextBrightNeon = Color(0xFFE19CD4)   // Bright lavender-pink title accents
    val colorTextBodyWhite = Color(0xFFFFFFFF)    // Classic crisp story text white

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colorBackground)
    ) {
        // Top Header Status Panel
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            color = colorMenuBackground,
            shape = RoundedCornerShape(4.dp),
            border = BorderStroke(1.dp, colorBorderActive)
        ) {
            Row(
                modifier = Modifier
                    .padding(12.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = boss.name.ifEmpty { "Unnamed Boss" },
                        color = colorTextBrightNeon, // 👾 Glowing Title
                        fontFamily = TitsFontFamily,   // 🔤 Clean Sans Type
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${boss.race} • ${boss.dungeonVoice}",
                        color = colorTextBodyWhite.copy(alpha = 0.6f),
                        fontFamily = TitsFontFamily,
                        fontSize = 12.sp
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Coins: ${viewModel.coins.value}",
                        color = colorTextBrightNeon,
                        fontFamily = TitsFontFamily,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Infamy: ${viewModel.infamy.value}",
                        color = Color(0xFFFF4FA8), // Neon Alert Pink
                        fontFamily = TitsFontFamily,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Chat text stream area
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(8.dp),
            state = listState,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(messages) { message ->
                ChatMessageBubble(
                    message = message, 
                    fontFamily = TitsFontFamily,
                    bodyColor = colorTextBodyWhite,
                    systemColor = colorTextBrightNeon,
                    panelColor = colorMenuBackground,
                    borderColor = colorBorderActive
                )
            }
            
            if (isLoading) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.Start
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = colorTextBrightNeon,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Generating response...",
                            color = colorTextBodyWhite.copy(alpha = 0.5f),
                            fontFamily = TitsFontFamily,
                            fontStyle = FontStyle.Italic,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }

        // Bottom User Input Interaction Area
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            color = colorMenuBackground,
            shape = RoundedCornerShape(4.dp),
            border = BorderStroke(1.dp, colorBorderActive)
        ) {
            Row(
                modifier = Modifier
                    .padding(8.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextField(
                    value = userInput,
                    onValueChange = { userInput = it },
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp),
                    placeholder = {
                        Text(
                            text = "Speak to the Dungeon...",
                            color = colorTextBodyWhite.copy(alpha = 0.3f),
                            fontFamily = TitsFontFamily,
                            fontSize = 13.sp
                        )
                    },
                    textStyle = TextStyle(
                        fontFamily = TitsFontFamily,
                        fontSize = 14.sp
                    ),
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
                    enabled = !isLoading && userInput.trim().isNotEmpty(),
                    modifier = Modifier.height(50.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colorMenuBackground,
                        contentColor = colorTextBrightNeon,
                        disabledContainerColor = colorBackground
                    ),
                    shape = RoundedCornerShape(4.dp),
                    border = BorderStroke(1.dp, if (userInput.trim().isNotEmpty()) colorTextBrightNeon else colorBorderActive)
                ) {
                    Text(
                        text = "Send", 
                        fontFamily = TitsFontFamily,
                        fontWeight = FontWeight.Bold,
                        color = if (userInput.trim().isNotEmpty()) colorTextBrightNeon else Color.Gray
                    )
                }
            }
        }
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
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        horizontalArrangement = if (isUserMessage) Arrangement.End else Arrangement.Start
    ) {
        Surface(
            modifier = Modifier
                .widthIn(max = 320.dp),
            // User gets a structured capsule panel; Dungeon narration presents as direct log output text
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

