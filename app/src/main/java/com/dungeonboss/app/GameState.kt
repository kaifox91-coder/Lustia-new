package com.dungeonboss.app

import android.content.Context
import androidx.core.content.edit
import android.content.SharedPreferences
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString

class GameState(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("dungeon_boss_game", Context.MODE_PRIVATE)
    private val json = Json { ignoreUnknownKeys = true }
    
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
        }
    }

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
        val entries = gameData.storyLog.takeLast(6) + entry
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
