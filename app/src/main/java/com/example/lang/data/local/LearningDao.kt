package com.example.lang.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface LessonDao {
    @Query("SELECT COUNT(*) FROM lessons")
    suspend fun lessonCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLessons(lessons: List<LessonEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFlashcards(cards: List<FlashcardEntity>)

    @Query(
        """
        SELECT lessons.id, lessons.title, lessons.type, lessons.orderIndex, lessons.description,
            COALESCE(lesson_progress.completed, 0) AS completed,
            COUNT(flashcards.id) AS cardCount
        FROM lessons
        LEFT JOIN lesson_progress ON lesson_progress.lessonId = lessons.id
        LEFT JOIN flashcards ON flashcards.lessonId = lessons.id
        GROUP BY lessons.id
        ORDER BY lessons.orderIndex
        """,
    )
    fun observeLessons(): Flow<List<LessonWithProgress>>

    @Query("SELECT * FROM flashcards WHERE lessonId = :lessonId ORDER BY orderIndex")
    fun observeCardsForLesson(lessonId: String): Flow<List<FlashcardEntity>>
}

@Dao
interface ProgressDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertReviewStates(states: List<ReviewStateEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertReviewState(state: ReviewStateEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertLessonProgress(progress: LessonProgressEntity)

    @Query("SELECT * FROM review_states WHERE cardId = :cardId")
    suspend fun getReviewState(cardId: String): ReviewStateEntity?

    @Query(
        """
        SELECT flashcards.id AS cardId, flashcards.lessonId, flashcards.category, flashcards.frontText,
            flashcards.backText, flashcards.reading, flashcards.example, review_states.easeFactor,
            review_states.intervalDays, review_states.reviewCount, review_states.dueAtEpochDay,
            review_states.lastReviewedEpochDay, review_states.correctCount, review_states.wrongCount,
            review_states.learned
        FROM flashcards
        INNER JOIN review_states ON review_states.cardId = flashcards.id
        WHERE review_states.dueAtEpochDay <= :todayEpochDay
        ORDER BY review_states.dueAtEpochDay, flashcards.orderIndex
        LIMIT :limit
        """,
    )
    fun observeDueCards(todayEpochDay: Long, limit: Int): Flow<List<ReviewCard>>

    @Query("SELECT COUNT(*) FROM review_states WHERE dueAtEpochDay <= :todayEpochDay")
    fun observeDueCount(todayEpochDay: Long): Flow<Int>

    @Query("SELECT COUNT(*) FROM review_states WHERE learned = 1")
    fun observeLearnedCount(): Flow<Int>

    @Query("SELECT COALESCE(SUM(xpEarned), 0) FROM daily_sessions")
    fun observeTotalXp(): Flow<Int>

    @Query("SELECT * FROM daily_sessions ORDER BY dateEpochDay DESC")
    fun observeSessions(): Flow<List<DailySessionEntity>>

    @Query("SELECT * FROM daily_sessions WHERE dateEpochDay = :dateEpochDay")
    suspend fun getDailySession(dateEpochDay: Long): DailySessionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertDailySession(session: DailySessionEntity)

    @Query("SELECT COUNT(*) FROM lesson_progress WHERE completed = 1")
    fun observeCompletedLessonCount(): Flow<Int>
}
