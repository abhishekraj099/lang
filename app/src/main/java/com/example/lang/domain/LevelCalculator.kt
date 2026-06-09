package com.example.lang.domain

data class LevelInfo(
    val name: String,
    val currentXp: Int,
    val levelStartXp: Int,
    val nextLevelXp: Int,
) {
    val progress: Float
        get() = ((currentXp - levelStartXp).toFloat() / (nextLevelXp - levelStartXp))
            .coerceIn(0f, 1f)
}

object LevelCalculator {
    private val levels = listOf(
        0 to "Beginner",
        500 to "Elementary",
        2_000 to "Explorer",
        5_000 to "Conversational",
        10_000 to "Confident",
    )

    fun fromXp(totalXp: Int): LevelInfo {
        val current = levels.last { totalXp >= it.first }
        val next = levels.firstOrNull { totalXp < it.first } ?: (current.first + 10_000 to "Mastery")
        return LevelInfo(
            name = current.second,
            currentXp = totalXp,
            levelStartXp = current.first,
            nextLevelXp = next.first,
        )
    }
}
