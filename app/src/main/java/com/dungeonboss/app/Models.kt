package com.dungeonboss.app

import kotlinx.serialization.Serializable
import org.json.JSONArray
import org.json.JSONObject

// 1. Independent data class MUST be declared first
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

// 2. Parsed second so the compiler already understands BossStats
@Serializable
data class Boss(
    val id: String = "boss",
    val defense: Int = 0,
    val abilities: List<String> = emptyList(),
    val legendaryActions: Int = 0,
    val customRules: List<String> = emptyList(),
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
    val stats: BossStats = BossStats(), // ⬅️ Safely resolved now
    val hp: Int = 100,
    val maxHp: Int = 100,
    val stamina: Int = 100,
    val maxStamina: Int = 100,
    val mana: Int = 100,
    val maxMana: Int = 100,
    val dungeonVoice: String = "Chronicle"
) {
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

// 3. Independent utility models placed at the bottom
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
