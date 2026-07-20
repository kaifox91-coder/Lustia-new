package com.dungeonboss.app

import kotlinx.serialization.Serializable
import org.json.JSONArray
import org.json.JSONObject

@Serializable
data class BossStats(
    val size: String = "",
    val physique: String = "",
    val resilience: String = "",
    val willpower: String = "",
    val speed: String = "",
    val agility: String = "",
    val reflexes: String = "",
    val weaponHandling: String = "",
    val tactics: String = "",
    val aim: String = "",
    val charisma: String = "",
    val deception: String = "",
    val seduction: String = "",
    val manipulation: String = "",
    val trapCraft: String = "",
    val floorKnowledge: String = "",
    val minionCommand: String = "",
    val arcana: String = "",
    val manaSurge: String = ""
)

@Serializable
data class Boss(
    // Combat Tracking Properties
    val id: String = "boss",
    val defense: Int = 0,
    val abilities: List<String> = emptyList(),
    val legendaryActions: Int = 0,
    val customRules: List<String> = emptyList(),
    
    // Core RPG Properties
    val name: String = "Unnamed Boss",
    val race: String = "",
    val age: Int = 0,
    val height: String = "",
    val gender: String = "",
    val appearance: String = "",
    val setting: String = "",
    val floorTheme: String = "",
    val bosspower: String = "",
    val skills: List<String> = listOf(),
    val techniques: List<String> = listOf(),
    val spells: List<String> = listOf(),
    val stats: BossStats = BossStats(),
    val hp: Int = 100,
    val maxHp: Int = 100,
    val stamina: Int = 100,
    val maxStamina: Int = 100,
    val mana: Int = 100,
    val maxMana: Int = 100,
    val dungeonVoice: String = "Chronicle"
) {
    // Keeps your original JSON fallback compatible for combat nodes
    fun toJson(): JSONObject {
        val j = JSONObject()
        j.put("id", id)
        j.put("name", name)
        j.put("hp", hp)
        j.put("maxHp", maxHp)
        j.put("defense", defense)
        j.put("abilities", JSONArray(abilities))
        j.put("legendaryActions", legendaryActions)
        j.put("customRules", JSONArray(customRules))
        return j
    }

    companion object {
        fun fromJson(j: JSONObject): Boss {
            val abilities = j.optJSONArray("abilities")?.let { arr ->
                List(arr.length()) { i -> arr.optString(i) }
            } ?: emptyList()

            val customRules = j.optJSONArray("customRules")?.let { arr ->
                List(arr.length()) { i -> arr.optString(i) }
            } ?: emptyList()

            return Boss(
                id = j.optString("id", "boss"),
                name = j.optString("name", "Unnamed Boss"),
                hp = j.optInt("hp", 100),
                maxHp = j.optInt("maxHp", j.optInt("hp", 100)),
                defense = j.optInt("defense", 0),
                abilities = abilities,
                legendaryActions = j.optInt("legendaryActions", 0),
                customRules = customRules
            )
        }
    }
}
    val age: Int = 0,
    val height: String = "",
    val gender: String = "",
    val appearance: String = "",
    val setting: String = "",
    val floorTheme: String = "",
    val bosspower: String = "",
    val skills: List<String> = listOf(),
    val techniques: List<String> = listOf(),
    val spells: List<String> = listOf(),
    val stats: BossStats = BossStats(),
    val hp: Int = 100,
    val maxHp: Int = 100,
    val stamina: Int = 100,
    val maxStamina: Int = 100,
    val mana: Int = 100,
    val maxMana: Int = 100,
    val dungeonVoice: String = "Chronicle"
)

@Serializable
data class Minion(
    val name: String,
    val type: String,
    val hp: Int,
    val maxHp: Int,
    val status: String = "active"
)

@Serializable
data class GameData(
    val boss: Boss = Boss(),
    val coins: Int = 50,
    val infamy: Int = 0,
    val minions: List<Minion> = listOf(),
    val activeTraps: List<String> = listOf(),
    val bonds: Map<String, Int> = mapOf(),
    val storyLog: List<String> = listOf(),
    val lastUpdated: Long = System.currentTimeMillis(),
    val creationStep: Int = 0
)

class GameState(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("dungeon_boss_game", Context.MODE_PRIVATE)
    private val json = Json { ignoreUnknownKeys = true }
    
    private var gameData: GameData = loadGame()

    fun loadGame(): GameData {
        return try {
            val json = prefs.getString("game_data", null)
            if (json != null) {
                this.json.decodeFromString<GameData>(json)
            } else {
                GameData()
            }
        } catch (e: Exception) {
            GameData()
        }
    }

    fun saveGame() {
        val json = json.encodeToString(GameData.serializer(), gameData)
        prefs.edit {
            putString("game_data", json)
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
