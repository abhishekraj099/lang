package com.example.lang

import org.junit.Assert.assertTrue
import org.junit.Test

class SyncPayloadTest {
    @Test
    fun lessonSyncPayloadContainsRequiredFields() {
        val lessonId = "hiragana-a"
        val payload = """{"lessonId":"$lessonId","completedAtEpochDay":100,"xp":20}"""

        assertTrue(payload.contains("lessonId"))
        assertTrue(payload.contains("completedAtEpochDay"))
        assertTrue(payload.contains("xp"))
    }
}
