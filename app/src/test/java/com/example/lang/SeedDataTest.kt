package com.example.lang

import com.example.lang.data.SeedData
import org.junit.Assert.assertTrue
import org.junit.Test

class SeedDataTest {
    @Test
    fun seedContentIncludesJapaneseStarterPack() {
        assertTrue(SeedData.lessons.size >= 10)
        assertTrue(SeedData.flashcards.size >= 50)
        assertTrue(SeedData.flashcards.any { it.frontText == "\u3042" && it.backText == "a" })
        assertTrue(SeedData.flashcards.any { it.lessonId == "greetings" })
        assertTrue(SeedData.flashcards.any { it.lessonId == "particles-wa-o" })
    }

    @Test
    fun everyCardBelongsToSeedLesson() {
        val lessonIds = SeedData.lessons.map { it.id }.toSet()

        assertTrue(SeedData.flashcards.all { it.lessonId in lessonIds })
    }
}
