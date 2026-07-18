package com.dungeonboss.app

import org.json.JSONArray
import org.json.JSONObject

data class Boss(
    val id: String = "boss",
    val name: String = "Unnamed Boss",
    val hp: Int = 100,
    val maxHp: Int = 100,
    val defense: Int = 0,
    val abilities: List<String> = emptyList(),
    val legendaryActions: Int = 0,
    val customRules: List<String> = emptyList()
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
