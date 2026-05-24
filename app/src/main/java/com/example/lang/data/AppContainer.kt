package com.example.lang.data

import android.content.Context
import com.example.lang.data.local.AppDatabase
import com.example.lang.data.preferences.UserPreferencesStore

class AppContainer(context: Context) {
    private val database = AppDatabase.create(context)

    val preferencesStore = UserPreferencesStore(context)
    val learningRepository = LearningRepository(
        lessonDao = database.lessonDao(),
        progressDao = database.progressDao(),
    )
}
