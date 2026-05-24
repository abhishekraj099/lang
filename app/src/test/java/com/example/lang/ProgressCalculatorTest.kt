package com.example.lang

import com.example.lang.data.local.DailySessionEntity
import com.example.lang.domain.ProgressCalculator
import org.junit.Assert.assertEquals
import org.junit.Test

class ProgressCalculatorTest {
    @Test
    fun countsConsecutiveActiveDaysIncludingToday() {
        val sessions = listOf(
            DailySessionEntity(dateEpochDay = 10, xpEarned = 20),
            DailySessionEntity(dateEpochDay = 9, cardsReviewed = 3),
            DailySessionEntity(dateEpochDay = 8, lessonsCompleted = 1),
        )

        assertEquals(3, ProgressCalculator.currentStreak(sessions, todayEpochDay = 10))
    }

    @Test
    fun keepsStreakAliveWhenYesterdayWasActive() {
        val sessions = listOf(
            DailySessionEntity(dateEpochDay = 9, xpEarned = 20),
            DailySessionEntity(dateEpochDay = 8, xpEarned = 20),
        )

        assertEquals(2, ProgressCalculator.currentStreak(sessions, todayEpochDay = 10))
    }

    @Test
    fun stopsAtGap() {
        val sessions = listOf(
            DailySessionEntity(dateEpochDay = 10, xpEarned = 20),
            DailySessionEntity(dateEpochDay = 8, xpEarned = 20),
        )

        assertEquals(1, ProgressCalculator.currentStreak(sessions, todayEpochDay = 10))
    }
}
