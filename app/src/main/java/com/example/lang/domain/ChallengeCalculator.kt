package com.example.lang.domain

import com.example.lang.data.local.DailyChallengeEntity

object ChallengeCalculator {
    fun streak(challenges: List<DailyChallengeEntity>, todayEpochDay: Long): Int {
        val completedDays = challenges
            .filter { it.completed }
            .map { it.dateEpochDay }
            .toSet()

        var cursor = todayEpochDay
        if (cursor !in completedDays && cursor - 1 in completedDays) {
            cursor -= 1
        }

        var count = 0
        while (cursor in completedDays) {
            count += 1
            cursor -= 1
        }
        return count
    }
}
