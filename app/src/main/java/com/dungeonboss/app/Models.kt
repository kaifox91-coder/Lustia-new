package com.dungeonboss.app

import kotlinx.serialization.Serializable

@Serializable
data class BossStats(
    val size: String = "Average",
    val physique: String = "Fit",
    val resilience: String = "Tough",
    val willpower: String = "Steady",
    val speed: String = "Average",
    val agility: String = "Agile",
    val reflexes: String = "Sharp",
    val weaponHandling: String = "Skilled",
    val tactics: String = "Calculated",
    val aim: String = "Precise",
    val charisma: String = "Charming",
    val deception: String = "Slippery",
    val seduction: String = "Enticing",
    val manipulation: String = "Cunning",
    val trapCraft: String = "Expert",
    val floorKnowledge: String = "Intimate",
    val minionCommand: String = "Feared",
    val arcana: String = "Versed",
    val manaSurge: String = "Lake"
)

@Serializable
data class Boss(
    val name: String = "",
    val race: String = "",
    val age: Int = 18,
    val height: String = "",
    val gender: String = "",
    val appearance: String = "",
    val title: String = "",
    val setting: String = "",
    val floorTheme: String = "",
    val bosspower: String = "",
    val hp: Int = 100,
    val skills: List<String> = emptyList(),
    val techniques: List<String> = emptyList(),
    val spells: List<String> = emptyList(),
    val stats: BossStats = BossStats(),
    val dungeonVoice: String = "Chronicle"
)

@Serializable
data class Minion(
    val name: String,
    val hp: Int = 100,
    val status: String = "active"
)

@Serializable
data class GameData(
    val boss: Boss = Boss(),
    val coins: Int = 0,
    val infamy: Int = 0,
    val minions: List<Minion> = emptyList(),
    val storyLog: List<String> = emptyList(),
    val creationStep: Int = 0,
    val bonds: Map<String, Int> = emptyMap(),
    val lastUpdated: Long = System.currentTimeMillis()
)
