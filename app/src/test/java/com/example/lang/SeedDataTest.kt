package com.example.lang

import com.example.lang.data.SeedData
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SeedDataTest {
    @Test
    fun seedContentIncludesJapaneseStarterPack() {
        assertEquals(4, SeedData.lessons.size)
        assertTrue(SeedData.flashcards.size >= 15)
        assertTrue(SeedData.flashcards.any { it.frontText == "あ" && it.backText == "a" })
        assertTrue(SeedData.flashcards.any { it.lessonId == "greetings" })
    }

    @Test
    fun everyCardBelongsToSeedLesson() {
        val lessonIds = SeedData.lessons.map { it.id }.toSet()

        assertTrue(SeedData.flashcards.all { it.lessonId in lessonIds })
    }
}
