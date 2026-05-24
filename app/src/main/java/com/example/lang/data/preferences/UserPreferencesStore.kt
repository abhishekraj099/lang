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
)

class UserPreferencesStore(private val context: Context) {
    val preferences: Flow<UserPreferences> = context.userDataStore.data.map { values ->
        UserPreferences(
            onboardingComplete = values[ONBOARDING_COMPLETE] ?: false,
            selectedLanguage = values[SELECTED_LANGUAGE] ?: "ja",
            dailyGoalMinutes = values[DAILY_GOAL_MINUTES] ?: 10,
        )
    }

    suspend fun completeOnboarding(dailyGoalMinutes: Int) {
        context.userDataStore.edit { values ->
            values[ONBOARDING_COMPLETE] = true
            values[SELECTED_LANGUAGE] = "ja"
            values[DAILY_GOAL_MINUTES] = dailyGoalMinutes
        }
    }

    companion object {
        private val ONBOARDING_COMPLETE = booleanPreferencesKey("onboarding_complete")
        private val SELECTED_LANGUAGE = stringPreferencesKey("selected_language")
        private val DAILY_GOAL_MINUTES = intPreferencesKey("daily_goal_minutes")
    }
}
