package com.example.lang.domain

import com.example.lang.data.local.ReviewStateEntity
import kotlin.math.max
import kotlin.math.roundToInt

object ReviewScheduler {
    fun schedule(
        current: ReviewStateEntity,
        correct: Boolean,
        todayEpochDay: Long,
    ): ReviewStateEntity {
        if (!correct) {
            return current.copy(
                easeFactor = max(1.3, current.easeFactor - 0.2),
                intervalDays = 1,
                reviewCount = current.reviewCount + 1,
                dueAtEpochDay = todayEpochDay + 1,
                lastReviewedEpochDay = todayEpochDay,
                wrongCount = current.wrongCount + 1,
                learned = false,
            )
        }

        val nextInterval = when (current.reviewCount) {
            0 -> 1
            1 -> 3
            else -> max(4, (current.intervalDays * current.easeFactor).roundToInt())
        }
        val nextEase = max(1.3, current.easeFactor + 0.1)

        return current.copy(
            easeFactor = nextEase,
            intervalDays = nextInterval,
            reviewCount = current.reviewCount + 1,
            dueAtEpochDay = todayEpochDay + nextInterval,
            lastReviewedEpochDay = todayEpochDay,
            correctCount = current.correctCount + 1,
            learned = current.correctCount + 1 >= 2,
        )
    }
}
