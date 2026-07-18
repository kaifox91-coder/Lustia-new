package com.dungeonboss.app

import org.json.JSONArray
import org.json.JSONObject

data class Boss(
    val id: String,
    val name: String,
    val hp: Int,
    val maxHp: Int,
    val defense: Int,
    val abilities: List<String>,
    val legendaryActions: Int,
    val customRules: List<String>
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
                hp = j.optInt("hp", 0),
                maxHp = j.optInt("maxHp", j.optInt("hp", 0)),
                defense = j.optInt("defense", 0),
                abilities = abilities,
                legendaryActions = j.optInt("legendaryActions", 0),
                customRules = customRules
            )
        }
    }
}

data class GameSession(
    val id: String,
    val boss: Boss,
    val playerActions: List<String>,
    val dungeonResponses: List<String>,
    val currentRound: Int
) {
    fun toJson(): JSONObject {
        val j = JSONObject()
        j.put("id", id)
        j.put("boss", boss.toJson())
        j.put("playerActions", JSONArray(playerActions))
        j.put("dungeonResponses", JSONArray(dungeonResponses))
        j.put("currentRound", currentRound)
        return j
    }

    companion object {
        fun fromJson(j: JSONObject): GameSession {
            return GameSession(
                id = j.optString("id", "session"),
                boss = Boss.fromJson(j.optJSONObject("boss") ?: JSONObject()),
                playerActions = j.optJSONArray("playerActions")?.let { arr ->
                    List(arr.length()) { i -> arr.optString(i) }
                } ?: emptyList(),
                dungeonResponses = j.optJSONArray("dungeonResponses")?.let { arr ->
                    List(arr.length()) { i -> arr.optString(i) }
                } ?: emptyList(),
                currentRound = j.optInt("currentRound", 0)
            )
        }
    }
}
