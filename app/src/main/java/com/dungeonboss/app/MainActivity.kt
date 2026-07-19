package com.dungeonboss.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.graphics.Color

class MainActivity : ComponentActivity() {
    private lateinit var gameState: GameState
    private lateinit var chatViewModel: ChatViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        gameState = GameState(this)
        chatViewModel = ChatViewModel(this)

        setContent {
            MaterialTheme {
                Surface(color = Color(0xFF1A1A2E)) {
                    MainScreen(gameState, chatViewModel)
                }
            }
        }
    }
}

@Composable
fun MainScreen(gameState: GameState, chatViewModel: ChatViewModel) {
    var screenState by remember { mutableStateOf("check") } // "check", "creation", "chat"
    val boss = gameState.getBoss()

    // Check if boss exists
    LaunchedEffect(Unit) {
        screenState = if (boss.name.isEmpty()) {
            "creation"
        } else {
            "chat"
        }
    }

    when (screenState) {
        "creation" -> {
            CharacterCreationScreen(
                onCreationComplete = { newBoss ->
                    gameState.updateBoss(newBoss)
                    chatViewModel.updateBoss(newBoss)
                    screenState = "chat"
                },
                gameState = gameState
            )
        }
        "chat" -> {
            ChatScreen(chatViewModel)
        }
    }
}
