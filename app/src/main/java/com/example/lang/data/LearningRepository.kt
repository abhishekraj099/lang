package com.example.lang.data

import com.example.lang.data.local.DailySessionEntity
import com.example.lang.data.local.FlashcardEntity
import com.example.lang.data.local.LessonDao
import com.example.lang.data.local.LessonProgressEntity
import com.example.lang.data.local.LessonWithProgress
import com.example.lang.data.local.ProgressDao
import com.example.lang.data.local.ReviewCard
import com.example.lang.data.local.ReviewStateEntity
import com.example.lang.domain.DayClock
import com.example.lang.domain.ProgressCalculator
import com.example.lang.domain.ReviewScheduler
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

data class ProgressSummary(
    val totalXp: Int = 0,
    val learnedWords: Int = 0,
    val dueCount: Int = 0,
    val completedLessons: Int = 0,
    val streak: Int = 0,
)

class LearningRepository(
    private val lessonDao: LessonDao,
    private val progressDao: ProgressDao,
) {
    suspend fun seedIfNeeded() {
        if (lessonDao.lessonCount() > 0) return
        lessonDao.insertLessons(SeedData.lessons)
        lessonDao.insertFlashcards(SeedData.flashcards)
        progressDao.insertReviewStates(
            SeedData.flashcards.map { ReviewStateEntity(cardId = it.id) },
        )
    }

    fun observeLessons(): Flow<List<LessonWithProgress>> = lessonDao.observeLessons()

    fun observeCardsForLesson(lessonId: String): Flow<List<FlashcardEntity>> =
        lessonDao.observeCardsForLesson(lessonId)

    fun observeDueCards(limit: Int = 20): Flow<List<ReviewCard>> =
        progressDao.observeDueCards(todayEpochDay(), limit)

    fun observeProgressSummary(): Flow<ProgressSummary> = combine(
        progressDao.observeTotalXp(),
        progressDao.observeLearnedCount(),
        progressDao.observeDueCount(todayEpochDay()),
        progressDao.observeCompletedLessonCount(),
        progressDao.observeSessions(),
    ) { totalXp, learnedWords, dueCount, completedLessons, sessions ->
        ProgressSummary(
            totalXp = totalXp,
            learnedWords = learnedWords,
            dueCount = dueCount,
            completedLessons = completedLessons,
            streak = ProgressCalculator.currentStreak(sessions, todayEpochDay()),
        )
    }

    fun observeSessions(): Flow<List<DailySessionEntity>> = progressDao.observeSessions()

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
    }

    suspend fun recordReview(cardId: String, correct: Boolean) {
        val current = progressDao.getReviewState(cardId) ?: ReviewStateEntity(cardId = cardId)
        progressDao.upsertReviewState(
            ReviewScheduler.schedule(
                current = current,
                correct = correct,
                todayEpochDay = todayEpochDay(),
            ),
        )
        incrementToday(xp = if (correct) 8 else 3, cards = 1, minutes = 1)
    }

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
