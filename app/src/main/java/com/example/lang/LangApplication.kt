package com.example.lang

import android.app.Application
import com.example.lang.data.AppContainer

class LangApplication : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
