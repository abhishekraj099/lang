package com.example.lang

import com.example.lang.data.local.LessonWithProgress
import com.example.lang.domain.AchievementEvaluator
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AchievementEvaluatorTest {
    @Test
    fun unlocksFirstStepAndXpSpark() {
        val achievements = AchievementEvaluator.evaluate(
            totalXp = 100,
            learnedWords = 0,
            completedLessons = 1,
            streak = 0,
            cardsReviewed = 0,
            lessons = emptyList(),
        )

        assertTrue(achievements.first { it.id == "first_step" }.unlocked)
        assertTrue(achievements.first { it.id == "xp_spark" }.unlocked)
        assertFalse(achievements.first { it.id == "week_warrior" }.unlocked)
    }

    @Test
    fun unlocksHiraganaHeroWhenAllHiraganaLessonsAreComplete() {
        val lessons = listOf(
            LessonWithProgress("a", "Hiragana: A-row", "script", 1, "", true, 5),
            LessonWithProgress("k", "Hiragana: K-row", "script", 2, "", true, 5),
        )

        val achievements = AchievementEvaluator.evaluate(
            totalXp = 0,
            learnedWords = 0,
            completedLessons = 2,
            streak = 0,
            cardsReviewed = 0,
            lessons = lessons,
        )

        assertTrue(achievements.first { it.id == "hiragana_hero" }.unlocked)
    }
}
