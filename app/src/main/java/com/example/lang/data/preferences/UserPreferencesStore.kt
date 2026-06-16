package com.example.lang.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.userDataStore by preferencesDataStore(name = "user_preferences")

data class UserPreferences(
    val onboardingComplete: Boolean = false,
    val selectedLanguage: String = "ja",
    val dailyGoalMinutes: Int = 10,
    val learningGoal: String = "Travel",
    val placementLevel: String = "Absolute beginner",
    val displayName: String = "Guest Learner",
    val notificationsEnabled: Boolean = true,
    val themeMode: String = ThemeMode.System.value,
)

enum class ThemeMode(val value: String, val label: String) {
    System("system", "System"),
    Light("light", "Light"),
    Dark("dark", "Dark");

    companion object {
        fun fromValue(value: String): ThemeMode =
            entries.firstOrNull { it.value == value } ?: System
    }
}

class UserPreferencesStore(private val context: Context) {
    val preferences: Flow<UserPreferences> = context.userDataStore.data.map { values ->
        UserPreferences(
            onboardingComplete = values[ONBOARDING_COMPLETE] ?: false,
            selectedLanguage = values[SELECTED_LANGUAGE] ?: "ja",
            dailyGoalMinutes = values[DAILY_GOAL_MINUTES] ?: 10,
            learningGoal = values[LEARNING_GOAL] ?: "Travel",
            placementLevel = values[PLACEMENT_LEVEL] ?: "Absolute beginner",
            displayName = values[DISPLAY_NAME] ?: "Guest Learner",
            notificationsEnabled = values[NOTIFICATIONS_ENABLED] ?: true,
            themeMode = values[THEME_MODE] ?: ThemeMode.System.value,
        )
    }

    suspend fun completeOnboarding(
        dailyGoalMinutes: Int,
        learningGoal: String,
        placementLevel: String,
        displayName: String,
    ) {
        context.userDataStore.edit { values ->
            values[ONBOARDING_COMPLETE] = true
            values[SELECTED_LANGUAGE] = "ja"
            values[DAILY_GOAL_MINUTES] = dailyGoalMinutes
            values[LEARNING_GOAL] = learningGoal
            values[PLACEMENT_LEVEL] = placementLevel
            values[DISPLAY_NAME] = displayName.ifBlank { "Guest Learner" }
        }
    }

    suspend fun setNotificationsEnabled(enabled: Boolean) {
        context.userDataStore.edit { values ->
            values[NOTIFICATIONS_ENABLED] = enabled
        }
    }

    suspend fun setThemeMode(themeMode: ThemeMode) {
        context.userDataStore.edit { values ->
            values[THEME_MODE] = themeMode.value
        }
    }

    companion object {
        private val ONBOARDING_COMPLETE = booleanPreferencesKey("onboarding_complete")
        private val SELECTED_LANGUAGE = stringPreferencesKey("selected_language")
        private val DAILY_GOAL_MINUTES = intPreferencesKey("daily_goal_minutes")
        private val LEARNING_GOAL = stringPreferencesKey("learning_goal")
        private val PLACEMENT_LEVEL = stringPreferencesKey("placement_level")
        private val DISPLAY_NAME = stringPreferencesKey("display_name")
        private val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
        private val THEME_MODE = stringPreferencesKey("theme_mode")
    }
}
