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
    private val apiKey = "YOUR_GEMINI_API_KEY" // Replace with actual key from BuildConfig or secure storage
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
fun ChatMessageBubble(message: UIChatMessage) {
    val isUserMessage = message.role == "user"
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        horizontalArrangement = if (isUserMessage) Arrangement.End else Arrangement.Start
    ) {
        Surface(
            modifier = Modifier
                .widthIn(max = 300.dp)
                .padding(4.dp),
            color = when (message.role) {
                "user" -> Color(0xFF0F3460)
                "dungeon" -> Color(0xFF1B4965)
                else -> Color(0xFF8B0000)
            },
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = message.content,
                modifier = Modifier.padding(12.dp),
                color = when (message.role) {
                    "user" -> Color(0xFFFFFFFF)
                    "dungeon" -> Color(0xFFD4AF37)
                    else -> Color(0xFFFF6B6B)
                },
                fontSize = 13.sp,
                lineHeight = 18.sp
            )
        }
    }
}
