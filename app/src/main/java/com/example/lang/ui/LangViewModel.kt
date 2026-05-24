package com.example.lang.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.lang.data.LearningRepository
import com.example.lang.data.ProgressSummary
import com.example.lang.data.local.FlashcardEntity
import com.example.lang.data.local.ReviewCard
import com.example.lang.data.preferences.UserPreferences
import com.example.lang.data.preferences.UserPreferencesStore
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class LangViewModel(
    private val repository: LearningRepository,
    private val preferencesStore: UserPreferencesStore,
) : ViewModel() {
    val preferences: StateFlow<UserPreferences> = preferencesStore.preferences.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = UserPreferences(),
    )

    val lessons = repository.observeLessons().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList(),
    )

    val progress = repository.observeProgressSummary().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ProgressSummary(),
    )

    val dueCards: StateFlow<List<ReviewCard>> = repository.observeDueCards().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList(),
    )

    init {
        viewModelScope.launch {
            repository.seedIfNeeded()
        }
    }

    fun cardsForLesson(lessonId: String): StateFlow<List<FlashcardEntity>> =
        repository.observeCardsForLesson(lessonId).stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList(),
        )

    fun completeOnboarding(dailyGoalMinutes: Int) {
        viewModelScope.launch {
            preferencesStore.completeOnboarding(dailyGoalMinutes)
        }
    }

    fun completeLesson(lessonId: String) {
        viewModelScope.launch {
            repository.completeLesson(lessonId)
        }
    }

    fun recordReview(cardId: String, correct: Boolean) {
        viewModelScope.launch {
            repository.recordReview(cardId, correct)
        }
    }

    companion object {
        fun factory(
            repository: LearningRepository,
            preferencesStore: UserPreferencesStore,
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return LangViewModel(repository, preferencesStore) as T
            }
        }
    }
}
