package com.example.lang

import com.example.lang.data.local.ReviewStateEntity
import com.example.lang.domain.ReviewGrade
import com.example.lang.domain.ReviewScheduler
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ReviewSchedulerTest {
    @Test
    fun goodAnswerSchedulesFirstReviewForTomorrow() {
        val result = ReviewScheduler.schedule(
            current = ReviewStateEntity(cardId = "a"),
            grade = ReviewGrade.Good,
            todayEpochDay = 100,
        )

        assertEquals(1, result.reviewCount)
        assertEquals(1, result.intervalDays)
        assertEquals(101, result.dueAtEpochDay)
        assertEquals(1, result.correctCount)
        assertEquals(5.1, result.difficulty, 0.01)
        assertFalse(result.learned)
    }

    @Test
    fun repeatedGoodAnswersMarkCardLearned() {
        val first = ReviewScheduler.schedule(ReviewStateEntity(cardId = "a"), ReviewGrade.Good, 100)
        val second = ReviewScheduler.schedule(first, ReviewGrade.Good, 101)

        assertTrue(second.learned)
        assertTrue(second.stability >= first.stability)
        assertTrue(second.dueAtEpochDay > 101)
    }

    @Test
    fun easyAnswerIncreasesIntervalWithoutIncreasingDifficulty() {
        val result = ReviewScheduler.schedule(
            current = ReviewStateEntity(cardId = "a", stability = 1.0, difficulty = 5.0),
            grade = ReviewGrade.Easy,
            todayEpochDay = 100,
        )

        assertTrue(result.intervalDays >= 2)
        assertTrue(result.difficulty <= 5.0)
    }

    @Test
    fun againKeepsCardDueSoonAndNotLearned() {
        val result = ReviewScheduler.schedule(
            current = ReviewStateEntity(cardId = "a", correctCount = 2, learned = true),
            grade = ReviewGrade.Again,
            todayEpochDay = 100,
        )

        assertEquals(1, result.intervalDays)
        assertEquals(101, result.dueAtEpochDay)
        assertEquals(1, result.wrongCount)
        assertFalse(result.learned)
    }
}
