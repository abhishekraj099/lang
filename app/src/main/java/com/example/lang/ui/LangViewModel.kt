package com.example.lang.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.lang.data.ChallengeSummary
import com.example.lang.data.LearningRepository
import com.example.lang.data.ProgressSummary
import com.example.lang.data.auth.AuthRepository
import com.example.lang.data.auth.AuthUser
import com.example.lang.data.local.FlashcardEntity
import com.example.lang.data.local.ReviewCard
import com.example.lang.data.preferences.UserPreferences
import com.example.lang.data.preferences.UserPreferencesStore
import com.example.lang.data.preferences.ThemeMode
import com.example.lang.domain.ReviewGrade
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class LangViewModel(
    private val repository: LearningRepository,
    private val preferencesStore: UserPreferencesStore,
    private val authRepository: AuthRepository,
) : ViewModel() {
    var authUser: AuthUser? by mutableStateOf(authRepository.currentUser)
        private set

    var authLoading: Boolean by mutableStateOf(false)
        private set

    var authError: String? by mutableStateOf(null)
        private set

    var guestMode: Boolean by mutableStateOf(false)
        private set

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

    val challengeSummary: StateFlow<ChallengeSummary> = repository.observeChallengeSummary().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ChallengeSummary(),
    )

    val allCards: StateFlow<List<FlashcardEntity>> = repository.observeAllCards().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList(),
    )

    init {
        viewModelScope.launch {
            repository.seedIfNeeded()
        }
        viewModelScope.launch {
            authRepository.observeAuthUser().collect { user ->
                authUser = user
            }
        }
    }

    fun cardsForLesson(lessonId: String): StateFlow<List<FlashcardEntity>> =
        repository.observeCardsForLesson(lessonId).stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList(),
        )

    fun quizCardsForLesson(lessonId: String): StateFlow<List<FlashcardEntity>> =
        repository.observeInterleavedCardsForLesson(lessonId).stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList(),
        )

    fun completeOnboarding(
        dailyGoalMinutes: Int,
        learningGoal: String,
        placementLevel: String,
        displayName: String,
    ) {
        viewModelScope.launch {
            preferencesStore.completeOnboarding(dailyGoalMinutes, learningGoal, placementLevel, displayName)
        }
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferencesStore.setNotificationsEnabled(enabled)
        }
    }

    fun setThemeMode(themeMode: ThemeMode) {
        viewModelScope.launch {
            preferencesStore.setThemeMode(themeMode)
        }
    }

    fun completeLesson(lessonId: String) {
        viewModelScope.launch {
            repository.completeLesson(lessonId)
        }
    }

    fun recordReview(cardId: String, grade: ReviewGrade) {
        viewModelScope.launch {
            repository.recordReview(cardId, grade)
        }
    }

    fun completeDailyChallenge(correctAnswers: Int, answered: Int) {
        viewModelScope.launch {
            repository.completeDailyChallenge(correctAnswers, answered)
        }
    }

    fun continueAsGuest() {
        guestMode = true
        authError = null
    }

    fun signIn(email: String, password: String) {
        viewModelScope.launch {
            authLoading = true
            authError = null
            runCatching { authRepository.signIn(email.trim(), password) }
                .onSuccess {
                    authUser = it
                    guestMode = false
                }
                .onFailure { authError = it.message ?: "Unable to sign in." }
            authLoading = false
        }
    }

    fun signInWithGoogle(idToken: String) {
        viewModelScope.launch {
            authLoading = true
            authError = null
            runCatching { authRepository.signInWithGoogle(idToken) }
                .onSuccess {
                    authUser = it
                    guestMode = false
                }
                .onFailure { authError = it.message ?: "Unable to sign in with Google." }
            authLoading = false
        }
    }

    fun signUp(email: String, password: String, displayName: String) {
        viewModelScope.launch {
            authLoading = true
            authError = null
            runCatching { authRepository.signUp(email.trim(), password, displayName.trim()) }
                .onSuccess {
                    authUser = it
                    guestMode = false
                }
                .onFailure { authError = it.message ?: "Unable to create account." }
            authLoading = false
        }
    }

    fun signOut() {
        authRepository.signOut()
        authUser = null
        guestMode = false
    }

    companion object {
        fun factory(
            repository: LearningRepository,
            preferencesStore: UserPreferencesStore,
            authRepository: AuthRepository,
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return LangViewModel(repository, preferencesStore, authRepository) as T
            }
        }
    }
}
