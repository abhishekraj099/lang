package com.example.lang

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LangFlowTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun onboardingCanReachHomeAndOpenLesson() {
        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodesWithText("Start learning").fetchSemanticsNodes().isNotEmpty() ||
                composeRule.onAllNodesWithText("Today").fetchSemanticsNodes().isNotEmpty()
        }

        if (composeRule.onAllNodesWithText("Start learning").fetchSemanticsNodes().isNotEmpty()) {
            composeRule.onNodeWithText("Start learning").performClick()
        }

        composeRule.onNodeWithText("Today").assertIsDisplayed()
        composeRule.onNodeWithText("Start lesson", substring = true).performClick()
        composeRule.onNodeWithText("Learn").assertIsDisplayed()
    }
}
