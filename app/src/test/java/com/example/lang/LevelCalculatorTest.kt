package com.example.lang

import com.example.lang.domain.LevelCalculator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LevelCalculatorTest {
    @Test
    fun startsAtBeginner() {
        val level = LevelCalculator.fromXp(0)

        assertEquals("Beginner", level.name)
        assertEquals(500, level.nextLevelXp)
        assertEquals(0f, level.progress, 0.001f)
    }

    @Test
    fun movesIntoElementaryAtFiveHundredXp() {
        val level = LevelCalculator.fromXp(750)

        assertEquals("Elementary", level.name)
        assertEquals(2_000, level.nextLevelXp)
        assertTrue(level.progress > 0f)
    }
}
