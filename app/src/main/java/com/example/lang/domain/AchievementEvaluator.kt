package com.example.lang.domain

import com.example.lang.data.local.LessonWithProgress

data class Achievement(
    val id: String,
    val title: String,
    val description: String,
    val unlocked: Boolean,
    val progressText: String,
)

object AchievementEvaluator {
    fun evaluate(
        totalXp: Int,
        learnedWords: Int,
        completedLessons: Int,
        streak: Int,
        cardsReviewed: Int,
        lessons: List<LessonWithProgress>,
    ): List<Achievement> {
        val completedHiragana = lessons.count { it.completed && it.title.startsWith("Hiragana") }
        val hiraganaTotal = lessons.count { it.title.startsWith("Hiragana") }.coerceAtLeast(1)

        return listOf(
            Achievement(
                id = "first_step",
                title = "First Step",
                description = "Complete your first lesson.",
                unlocked = completedLessons >= 1,
                progressText = "${completedLessons.coerceAtMost(1)}/1",
            ),
            Achievement(
                id = "hiragana_hero",
                title = "Hiragana Hero",
                description = "Complete all starter Hiragana rows.",
                unlocked = completedHiragana >= hiraganaTotal,
                progressText = "$completedHiragana/$hiraganaTotal",
            ),
            Achievement(
                id = "review_builder",
                title = "Review Builder",
                description = "Review 50 cards with spaced repetition.",
                unlocked = cardsReviewed >= 50,
                progressText = "${cardsReviewed.coerceAtMost(50)}/50",
            ),
            Achievement(
                id = "xp_spark",
                title = "XP Spark",
                description = "Earn your first 100 XP.",
                unlocked = totalXp >= 100,
                progressText = "${totalXp.coerceAtMost(100)}/100 XP",
            ),
            Achievement(
                id = "word_collector",
                title = "Word Collector",
                description = "Learn 25 cards.",
                unlocked = learnedWords >= 25,
                progressText = "${learnedWords.coerceAtMost(25)}/25",
            ),
            Achievement(
                id = "week_warrior",
                title = "Week Warrior",
                description = "Keep a 7 day streak.",
                unlocked = streak >= 7,
                progressText = "${streak.coerceAtMost(7)}/7 days",
            ),
        )
    }
}
