package com.example.lang

import com.example.lang.data.preferences.ThemeMode
import org.junit.Assert.assertEquals
import org.junit.Test

class ThemeModeTest {
    @Test
    fun parsesKnownThemeValues() {
        assertEquals(ThemeMode.System, ThemeMode.fromValue("system"))
        assertEquals(ThemeMode.Light, ThemeMode.fromValue("light"))
        assertEquals(ThemeMode.Dark, ThemeMode.fromValue("dark"))
    }

    @Test
    fun unknownThemeFallsBackToSystem() {
        assertEquals(ThemeMode.System, ThemeMode.fromValue("unknown"))
    }
}
