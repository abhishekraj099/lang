package com.example.lang.domain

import com.example.lang.data.local.ReviewStateEntity
import kotlin.math.exp
import kotlin.math.max
import kotlin.math.roundToInt

object ReviewScheduler {
    fun schedule(
        current: ReviewStateEntity,
        grade: ReviewGrade,
        todayEpochDay: Long,
    ): ReviewStateEntity {
        val daysSinceReview = current.lastReviewedEpochDay
            ?.let { max(0.0, (todayEpochDay - it).toDouble()) }
            ?: 0.0
        val retrievability = if (current.stability <= 0.0) {
            1.0
        } else {
            exp(-daysSinceReview / current.stability).coerceIn(0.0, 1.0)
        }

        val gradeOffset = grade.value - 3
        val nextDifficulty = (current.difficulty + 0.1 - gradeOffset * (0.08 + gradeOffset * 0.02))
            .coerceIn(1.0, 10.0)
        val nextStability = when (grade) {
            ReviewGrade.Again -> 0.2
            ReviewGrade.Hard -> max(0.5, current.stability * 0.8)
            ReviewGrade.Good -> max(1.0, current.stability * (1.0 + 2.5 * (1.0 - retrievability)))
            ReviewGrade.Easy -> max(2.0, current.stability * (1.0 + 3.5 * (1.0 - retrievability)))
        }
        val intervalDays = when (grade) {
            ReviewGrade.Again -> 1
            ReviewGrade.Hard -> max(1, nextStability.roundToInt())
            ReviewGrade.Good -> max(1, nextStability.roundToInt())
            ReviewGrade.Easy -> max(2, nextStability.roundToInt())
        }

        return current.copy(
            difficulty = nextDifficulty,
            stability = nextStability,
            retrievability = exp(-1.0 / nextStability).coerceIn(0.0, 1.0),
            easeFactor = (11.0 - nextDifficulty).coerceIn(1.3, 3.0),
            intervalDays = intervalDays,
            reviewCount = current.reviewCount + 1,
            dueAtEpochDay = todayEpochDay + intervalDays,
            lastReviewedEpochDay = todayEpochDay,
            correctCount = if (grade == ReviewGrade.Again) current.correctCount else current.correctCount + 1,
            wrongCount = if (grade == ReviewGrade.Again) current.wrongCount + 1 else current.wrongCount,
            learned = grade != ReviewGrade.Again && current.correctCount + 1 >= 2,
        )
    }
}
