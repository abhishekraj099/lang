package com.example.lang.data.local

data class LessonWithProgress(
    val id: String,
    val title: String,
    val type: String,
    val orderIndex: Int,
    val description: String,
    val completed: Boolean,
    val cardCount: Int,
)

data class ReviewCard(
    val cardId: String,
    val lessonId: String,
    val category: String,
    val frontText: String,
    val backText: String,
    val reading: String,
    val example: String,
    val easeFactor: Double,
    val difficulty: Double,
    val stability: Double,
    val retrievability: Double,
    val intervalDays: Int,
    val reviewCount: Int,
    val dueAtEpochDay: Long,
    val lastReviewedEpochDay: Long?,
    val correctCount: Int,
    val wrongCount: Int,
    val learned: Boolean,
)
