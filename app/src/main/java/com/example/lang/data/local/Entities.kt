package com.example.lang.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "lessons", indices = [Index("orderIndex"), Index("type")])
data class LessonEntity(
    @PrimaryKey val id: String,
    val title: String,
    val type: String,
    val orderIndex: Int,
    val description: String,
)

@Entity(
    tableName = "flashcards",
    indices = [
        Index("lessonId"),
        Index("category"),
        Index(value = ["lessonId", "category"]),
    ],
    foreignKeys = [
        ForeignKey(
            entity = LessonEntity::class,
            parentColumns = ["id"],
            childColumns = ["lessonId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
)
data class FlashcardEntity(
    @PrimaryKey val id: String,
    val lessonId: String,
    val category: String,
    val frontText: String,
    val backText: String,
    val reading: String,
    val example: String,
    val orderIndex: Int,
)

@Entity(
    tableName = "review_states",
    indices = [
        Index("dueAtEpochDay"),
        Index("learned"),
        Index(value = ["dueAtEpochDay", "learned"]),
    ],
)
data class ReviewStateEntity(
    @PrimaryKey val cardId: String,
    val easeFactor: Double = 2.5,
    val difficulty: Double = 5.0,
    val stability: Double = 1.0,
    val retrievability: Double = 1.0,
    val intervalDays: Int = 0,
    val reviewCount: Int = 0,
    val dueAtEpochDay: Long = 0,
    val lastReviewedEpochDay: Long? = null,
    val correctCount: Int = 0,
    val wrongCount: Int = 0,
    val learned: Boolean = false,
)

@Entity(tableName = "lesson_progress")
data class LessonProgressEntity(
    @PrimaryKey val lessonId: String,
    val completed: Boolean,
    val completedAtEpochDay: Long,
)

@Entity(tableName = "daily_sessions")
data class DailySessionEntity(
    @PrimaryKey val dateEpochDay: Long,
    val xpEarned: Int = 0,
    val cardsReviewed: Int = 0,
    val lessonsCompleted: Int = 0,
    val minutesSpent: Int = 0,
)

@Entity(tableName = "daily_challenges")
data class DailyChallengeEntity(
    @PrimaryKey val dateEpochDay: Long,
    val score: Int,
    val correctAnswers: Int,
    val answered: Int,
    val completed: Boolean,
)

@Entity(tableName = "sync_outbox", indices = [Index("synced"), Index("createdAtEpochMillis")])
data class SyncOutboxEntity(
    @PrimaryKey val id: String,
    val eventType: String,
    val payloadJson: String,
    val createdAtEpochMillis: Long,
    val synced: Boolean = false,
)
