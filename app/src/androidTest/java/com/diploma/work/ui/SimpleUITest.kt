package com.diploma.work.ui

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SimpleUITest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testCanCreateComposeRule() {
        assert(composeTestRule != null)
    }

    @Test
    fun testBasicComposeInteraction() {
        composeTestRule.setContent {
            androidx.compose.material3.Text("Hello World")
        }

        composeTestRule.onNodeWithText("Hello World").assertIsDisplayed()
    }
}
