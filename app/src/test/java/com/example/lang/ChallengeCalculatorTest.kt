package com.example.lang

import com.example.lang.data.local.DailyChallengeEntity
import com.example.lang.domain.ChallengeCalculator
import org.junit.Assert.assertEquals
import org.junit.Test

class ChallengeCalculatorTest {
    @Test
    fun countsChallengeStreakIncludingToday() {
        val challenges = listOf(
            DailyChallengeEntity(10, score = 120, correctAnswers = 10, answered = 20, completed = true),
            DailyChallengeEntity(9, score = 90, correctAnswers = 7, answered = 20, completed = true),
        )

        assertEquals(2, ChallengeCalculator.streak(challenges, todayEpochDay = 10))
    }

    @Test
    fun keepsChallengeStreakAliveFromYesterday() {
        val challenges = listOf(
            DailyChallengeEntity(9, score = 90, correctAnswers = 7, answered = 20, completed = true),
            DailyChallengeEntity(8, score = 80, correctAnswers = 6, answered = 20, completed = true),
        )

        assertEquals(2, ChallengeCalculator.streak(challenges, todayEpochDay = 10))
    }
}
