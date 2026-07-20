package com.dungeonboss.app

import kotlinx.serialization.Serializable

@Serializable
data class Boss(
    val name: String = "",
    val title: String = "",
    val hp: Int = 100
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
    val lastUpdated: Long = 0L
)
