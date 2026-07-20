package com.dungeonboss.app

import android.content.Context
import androidx.core.content.edit
import android.content.SharedPreferences
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString

class GameState(private val context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("dungeon_boss_game", Context.MODE_PRIVATE)
    private val json = Json { ignoreUnknownKeys = true }

    private companion object {
        const val MAX_STORY_ENTRIES = 500
    }
    
    private var gameData: GameData = loadGame()

    fun loadGame(): GameData {
        return try {
            val jsonStr = prefs.getString("game_data", null)
            if (jsonStr != null) {
                json.decodeFromString<GameData>(jsonStr)
            } else {
                GameData()
            }
        } catch (e: Exception) {
            GameData()
        }
    }

    fun saveGame() {
        val jsonStr = json.encodeToString(gameData)
        prefs.edit {
            putString("game_data", jsonStr)
            
            // If the character has a name, automatically back it up in their own profile slot
            if (gameData.boss.name.isNotEmpty()) {
                val slotKey = "slot_${gameData.boss.name.replace(Regex("[^a-zA-Z0-9]"), "_")}"
                putString(slotKey, jsonStr)
                
                // Track this name in our master list of saved characters
                val existingList = getAvailableSaves().toMutableSet()
                existingList.add(gameData.boss.name)
                putStringSet("saved_character_names_list", existingList)
            }
        }
    }

    // 📱 CALL THIS TO LOAD A SPECIFIC CHARACTER PROFILE FROM DISK
    fun loadCharacterProfile(bossName: String): Boolean {
        val slotKey = "slot_${bossName.replace(Regex("[^a-zA-Z0-9]"), "_")}"
        val jsonStr = prefs.getString(slotKey, null)
        return if (jsonStr != null) {
            try {
                gameData = json.decodeFromString<GameData>(jsonStr)
                prefs.edit { putString("game_data", jsonStr) } // Set as active game
                true
            } catch (e: Exception) {
                false
            }
        } else {
            false
        }
    }

    // Returns a list of all existing character profile slots sitting on the phone
    fun getAvailableSaves(): List<String> {
        val namesSet = prefs.getStringSet("saved_character_names_list", emptySet()) ?: emptySet()
        return namesSet.toList()
    }

    // --- YOUR EXISTING GAME LOGIC CODE REMEDIES UNTOUCHED ---
    fun updateBoss(boss: Boss) {
        gameData = gameData.copy(boss = boss)
        saveGame()
    }

    fun getBoss(): Boss = gameData.boss

    fun addCoins(amount: Int) {
        gameData = gameData.copy(coins = gameData.coins + amount)
        saveGame()
    }

    fun getCoins(): Int = gameData.coins

    fun addInfamy(amount: Int) {
        gameData = gameData.copy(infamy = gameData.infamy + amount)
        saveGame()
    }

    fun getInfamy(): Int = gameData.infamy

    fun addMinion(minion: Minion) {
        gameData = gameData.copy(minions = gameData.minions + minion)
        saveGame()
    }

    fun getMinions(): List<Minion> = gameData.minions

    fun updateMinion(name: String, hp: Int, status: String = "active") {
        val updated = gameData.minions.map {
            if (it.name == name) it.copy(hp = hp, status = status) else it
        }
        gameData = gameData.copy(minions = updated)
        saveGame()
    }

    fun addStoryEntry(entry: String) {
        val entries = (gameData.storyLog + entry).takeLast(MAX_STORY_ENTRIES)
        gameData = gameData.copy(storyLog = entries, lastUpdated = System.currentTimeMillis())
        saveGame()
    }

    fun getStoryLog(): List<String> = gameData.storyLog

    fun setCreationStep(step: Int) {
        gameData = gameData.copy(creationStep = step)
        saveGame()
    }

    fun getCreationStep(): Int = gameData.creationStep

    fun addBond(npcName: String, affection: Int) {
        val bonds = gameData.bonds.toMutableMap()
        bonds[npcName] = (bonds[npcName] ?: 0) + affection
        gameData = gameData.copy(bonds = bonds)
        saveGame()
    }

    fun getBonds(): Map<String, Int> = gameData.bonds

    fun resetGame() {
        gameData = GameData()
        saveGame()
    }

    fun getGameData(): GameData = gameData
}
