package com.example.lang

import com.example.lang.data.local.ReviewStateEntity
import com.example.lang.domain.ReviewScheduler
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ReviewSchedulerTest {
    @Test
    fun correctAnswerSchedulesFirstReviewForTomorrow() {
        val result = ReviewScheduler.schedule(
            current = ReviewStateEntity(cardId = "a"),
            correct = true,
            todayEpochDay = 100,
        )

        assertEquals(1, result.reviewCount)
        assertEquals(1, result.intervalDays)
        assertEquals(101, result.dueAtEpochDay)
        assertEquals(1, result.correctCount)
        assertFalse(result.learned)
    }

    @Test
    fun repeatedCorrectAnswersMarkCardLearned() {
        val first = ReviewScheduler.schedule(ReviewStateEntity(cardId = "a"), true, 100)
        val second = ReviewScheduler.schedule(first, true, 101)

        assertTrue(second.learned)
        assertEquals(3, second.intervalDays)
        assertEquals(104, second.dueAtEpochDay)
    }

    @Test
    fun wrongAnswerKeepsCardDueSoonAndNotLearned() {
        val result = ReviewScheduler.schedule(
            current = ReviewStateEntity(cardId = "a", correctCount = 2, learned = true),
            correct = false,
            todayEpochDay = 100,
        )

        assertEquals(1, result.intervalDays)
        assertEquals(101, result.dueAtEpochDay)
        assertEquals(1, result.wrongCount)
        assertFalse(result.learned)
    }
}
