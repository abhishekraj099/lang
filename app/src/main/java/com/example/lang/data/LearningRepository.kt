package com.example.lang.data

import com.example.lang.data.local.DailySessionEntity
import com.example.lang.data.local.DailyChallengeEntity
import com.example.lang.data.local.FlashcardEntity
import com.example.lang.data.local.LessonDao
import com.example.lang.data.local.LessonProgressEntity
import com.example.lang.data.local.LessonWithProgress
import com.example.lang.data.local.ProgressDao
import com.example.lang.data.local.ReviewCard
import com.example.lang.data.local.ReviewStateEntity
import com.example.lang.data.local.SyncOutboxEntity
import com.example.lang.domain.Achievement
import com.example.lang.domain.AchievementEvaluator
import com.example.lang.domain.ChallengeCalculator
import com.example.lang.domain.DayClock
import com.example.lang.domain.LevelCalculator
import com.example.lang.domain.LevelInfo
import com.example.lang.domain.ProgressCalculator
import com.example.lang.domain.ReviewGrade
import com.example.lang.domain.ReviewScheduler
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import java.util.UUID

data class ProgressSummary(
    val totalXp: Int = 0,
    val learnedWords: Int = 0,
    val dueCount: Int = 0,
    val completedLessons: Int = 0,
    val streak: Int = 0,
    val cardsReviewed: Int = 0,
    val level: LevelInfo = LevelCalculator.fromXp(0),
    val achievements: List<Achievement> = emptyList(),
    val totalMinutes: Int = 0,
    val recentActiveDays: List<Long> = emptyList(),
    val pendingSyncCount: Int = 0,
)

data class ChallengeSummary(
    val todayCompleted: Boolean = false,
    val todayScore: Int = 0,
    val bestScore: Int = 0,
    val streak: Int = 0,
    val totalCompleted: Int = 0,
)

class LearningRepository(
    private val lessonDao: LessonDao,
    private val progressDao: ProgressDao,
) {
    suspend fun seedIfNeeded() {
        lessonDao.insertLessons(SeedData.lessons)
        lessonDao.insertFlashcards(SeedData.flashcards)
        progressDao.insertReviewStates(
            SeedData.flashcards.map { ReviewStateEntity(cardId = it.id) },
        )
    }

    fun observeLessons(): Flow<List<LessonWithProgress>> = lessonDao.observeLessons()

    fun observeCardsForLesson(lessonId: String): Flow<List<FlashcardEntity>> =
        lessonDao.observeCardsForLesson(lessonId)

    fun observeAllCards(): Flow<List<FlashcardEntity>> = lessonDao.observeAllCards()

    fun observeInterleavedCardsForLesson(lessonId: String): Flow<List<FlashcardEntity>> =
        lessonDao.observeInterleavedCards(
            lessonId = lessonId,
            currentLimit = 6,
            reviewLimit = 4,
        )

    fun observeDueCards(limit: Int = 20): Flow<List<ReviewCard>> =
        progressDao.observeDueCards(todayEpochDay(), limit)

    fun observeProgressSummary(): Flow<ProgressSummary> = combine(
        progressDao.observeTotalXp(),
        progressDao.observeLearnedCount(),
        progressDao.observeDueCount(todayEpochDay()),
        progressDao.observeCompletedLessonCount(),
        progressDao.observeSessions(),
        progressDao.observeTotalCardsReviewed(),
        lessonDao.observeLessons(),
        progressDao.observeTotalMinutes(),
        progressDao.observePendingSyncCount(),
    ) { values ->
        val totalXp = values[0] as Int
        val learnedWords = values[1] as Int
        val dueCount = values[2] as Int
        val completedLessons = values[3] as Int
        @Suppress("UNCHECKED_CAST")
        val sessions = values[4] as List<DailySessionEntity>
        val cardsReviewed = values[5] as Int
        @Suppress("UNCHECKED_CAST")
        val lessons = values[6] as List<LessonWithProgress>
        val totalMinutes = values[7] as Int
        val pendingSyncCount = values[8] as Int
        val streak = ProgressCalculator.currentStreak(sessions, todayEpochDay())
        ProgressSummary(
            totalXp = totalXp,
            learnedWords = learnedWords,
            dueCount = dueCount,
            completedLessons = completedLessons,
            streak = streak,
            cardsReviewed = cardsReviewed,
            level = LevelCalculator.fromXp(totalXp),
            achievements = AchievementEvaluator.evaluate(
                totalXp = totalXp,
                learnedWords = learnedWords,
                completedLessons = completedLessons,
                streak = streak,
                cardsReviewed = cardsReviewed,
                lessons = lessons,
            ),
            totalMinutes = totalMinutes,
            recentActiveDays = sessions
                .filter { it.xpEarned > 0 || it.cardsReviewed > 0 || it.lessonsCompleted > 0 }
                .take(7)
                .map { it.dateEpochDay },
            pendingSyncCount = pendingSyncCount,
        )
    }

    fun observeSessions(): Flow<List<DailySessionEntity>> = progressDao.observeSessions()

    fun observeChallengeSummary(): Flow<ChallengeSummary> = progressDao.observeDailyChallenges()
        .combineWithToday()

    suspend fun completeLesson(lessonId: String) {
        val today = todayEpochDay()
        progressDao.upsertLessonProgress(
            LessonProgressEntity(
                lessonId = lessonId,
                completed = true,
                completedAtEpochDay = today,
            ),
        )
        incrementToday(xp = 20, lessons = 1, minutes = 5)
        enqueueSync(
            eventType = "lesson_completed",
            payloadJson = """{"lessonId":"${lessonId.escapeJson()}","completedAtEpochDay":$today,"xp":20}""",
        )
    }

    suspend fun recordReview(cardId: String, grade: ReviewGrade) {
        val current = progressDao.getReviewState(cardId) ?: ReviewStateEntity(cardId = cardId)
        progressDao.upsertReviewState(
            ReviewScheduler.schedule(
                current = current,
                grade = grade,
                todayEpochDay = todayEpochDay(),
            ),
        )
        incrementToday(xp = xpForGrade(grade), cards = 1, minutes = 1)
        enqueueSync(
            eventType = "review_recorded",
            payloadJson = """{"cardId":"${cardId.escapeJson()}","grade":"${grade.name}","reviewedAtEpochDay":${todayEpochDay()}}""",
        )
    }

    suspend fun completeDailyChallenge(correctAnswers: Int, answered: Int) {
        val today = todayEpochDay()
        val score = correctAnswers * 10 + answered
        progressDao.upsertDailyChallenge(
            DailyChallengeEntity(
                dateEpochDay = today,
                score = score,
                correctAnswers = correctAnswers,
                answered = answered,
                completed = answered > 0,
            ),
        )
        incrementToday(xp = score, cards = answered, minutes = 1)
        enqueueSync(
            eventType = "daily_challenge_completed",
            payloadJson = """{"dateEpochDay":$today,"score":$score,"correctAnswers":$correctAnswers,"answered":$answered}""",
        )
    }

    private fun Flow<List<DailyChallengeEntity>>.combineWithToday(): Flow<ChallengeSummary> =
        map { challenges ->
            val today = todayEpochDay()
            val todayChallenge = challenges.firstOrNull { it.dateEpochDay == today }
            ChallengeSummary(
                todayCompleted = todayChallenge?.completed ?: false,
                todayScore = todayChallenge?.score ?: 0,
                bestScore = challenges.maxOfOrNull { it.score } ?: 0,
                streak = ChallengeCalculator.streak(challenges, today),
                totalCompleted = challenges.count { it.completed },
            )
        }

    private fun xpForGrade(grade: ReviewGrade): Int = when (grade) {
        ReviewGrade.Again -> 3
        ReviewGrade.Hard -> 6
        ReviewGrade.Good -> 10
        ReviewGrade.Easy -> 14
    }

    private suspend fun enqueueSync(eventType: String, payloadJson: String) {
        progressDao.enqueueSyncEvent(
            SyncOutboxEntity(
                id = UUID.randomUUID().toString(),
                eventType = eventType,
                payloadJson = payloadJson,
                createdAtEpochMillis = System.currentTimeMillis(),
            ),
        )
    }

    private fun String.escapeJson(): String = replace("\\", "\\\\").replace("\"", "\\\"")

    private suspend fun incrementToday(
        xp: Int,
        cards: Int = 0,
        lessons: Int = 0,
        minutes: Int = 0,
    ) {
        val today = todayEpochDay()
        val current = progressDao.getDailySession(today) ?: DailySessionEntity(today)
        progressDao.upsertDailySession(
            current.copy(
                xpEarned = current.xpEarned + xp,
                cardsReviewed = current.cardsReviewed + cards,
                lessonsCompleted = current.lessonsCompleted + lessons,
                minutesSpent = current.minutesSpent + minutes,
            ),
        )
    }

    private fun todayEpochDay(): Long = DayClock.todayEpochDay()
}
