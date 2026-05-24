package com.example.lang.domain

import com.example.lang.data.local.DailySessionEntity

object ProgressCalculator {
    fun currentStreak(sessions: List<DailySessionEntity>, todayEpochDay: Long): Int {
        val activeDays = sessions
            .filter { it.xpEarned > 0 || it.cardsReviewed > 0 || it.lessonsCompleted > 0 }
            .map { it.dateEpochDay }
            .toSet()

        var cursor = todayEpochDay
        if (cursor !in activeDays && cursor - 1 in activeDays) {
            cursor -= 1
        }

        var streak = 0
        while (cursor in activeDays) {
            streak += 1
            cursor -= 1
        }
        return streak
    }
}
