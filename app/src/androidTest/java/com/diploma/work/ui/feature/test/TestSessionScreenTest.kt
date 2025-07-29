package com.diploma.work.ui.feature.test

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.diploma.work.ui.theme.DiplomaWorkTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TestSessionScreenTest {
    
    @get:Rule
    val composeTestRule = createComposeRule()
    
    @Test
    fun testSessionScreen_displaysQuestionText() {
        composeTestRule.setContent {
            DiplomaWorkTheme {
                TestSessionScreen(
                    sessionId = "test_session",
                    onNavigateBack = {},
                    onTestCompleted = { _, _ -> }
                )
            }
        }
        
        composeTestRule.onNodeWithTag("questionText")
            .assertIsDisplayed()
    }
    
    @Test
    fun testSessionScreen_multipleChoice_displaysOptions() {
        composeTestRule.setContent {
            DiplomaWorkTheme {
                TestSessionScreen(
                    sessionId = "test_session",
                    onNavigateBack = {},
                    onTestCompleted = { _, _ -> }
                )
            }
        }
        
        composeTestRule.onNodeWithTag("optionsList")
            .assertIsDisplayed()
    }
    
    @Test
    fun testSessionScreen_multipleChoice_optionSelection() {
        composeTestRule.setContent {
            DiplomaWorkTheme {
                TestSessionScreen(
                    sessionId = "test_session",
                    onNavigateBack = {},
                    onTestCompleted = { _, _ -> }
                )
            }
        }
        
        composeTestRule.onNodeWithTag("optionsList")
            .onChildren()
            .onFirst()
            .performClick()
    }
    
    @Test
    fun testSessionScreen_navigationButtons_displayed() {
        composeTestRule.setContent {
            DiplomaWorkTheme {
                TestSessionScreen(
                    sessionId = "test_session",
                    onNavigateBack = {},
                    onTestCompleted = { _, _ -> }
                )
            }
        }
        
        composeTestRule.onNodeWithTag("previousButton")
            .assertIsDisplayed()
        composeTestRule.onNodeWithTag("nextButton")
            .assertIsDisplayed()
    }
    
    @Test
    fun testSessionScreen_submitButton_navigation() {
        composeTestRule.setContent {
            DiplomaWorkTheme {
                TestSessionScreen(
                    sessionId = "test_session",
                    onNavigateBack = {},
                    onTestCompleted = { _, _ -> }
                )
            }
        }
        
        composeTestRule.onNodeWithTag("nextButton")
            .performClick()
        
        composeTestRule.onNodeWithTag("submitButton")
            .assertIsDisplayed()
    }
    
    @Test
    fun testSessionScreen_progressBar_showsProgress() {
        composeTestRule.setContent {
            DiplomaWorkTheme {
                TestSessionScreen(
                    sessionId = "test_session",
                    onNavigateBack = {},
                    onTestCompleted = { _, _ -> }
                )
            }
        }
        
        composeTestRule.onNodeWithTag("progressBar")
            .assertIsDisplayed()
        
        composeTestRule.onNodeWithTag("progressText")
            .assertIsDisplayed()
    }
    
    @Test
    fun testSessionScreen_timer_isDisplayed() {
        composeTestRule.setContent {
            DiplomaWorkTheme {
                TestSessionScreen(
                    sessionId = "test_session",
                    onNavigateBack = {},
                    onTestCompleted = { _, _ -> }
                )
            }
        }
        
        composeTestRule.onNodeWithTag("timer")
            .assertIsDisplayed()
    }
    
    @Test
    fun testSessionScreen_textQuestion_displaysTextInput() {
        composeTestRule.setContent {
            DiplomaWorkTheme {
                TestSessionScreen(
                    sessionId = "test_session",
                    onNavigateBack = {},
                    onTestCompleted = { _, _ -> }
                )
            }
        }
        
        composeTestRule.onNodeWithTag("nextButton")
            .performClick()
        
        composeTestRule.onNodeWithTag("textAnswerField")
            .assertIsDisplayed()
    }
    
    @Test
    fun testSessionScreen_textQuestion_acceptsInput() {
        composeTestRule.setContent {
            DiplomaWorkTheme {
                TestSessionScreen(
                    sessionId = "test_session",
                    onNavigateBack = {},
                    onTestCompleted = { _, _ -> }
                )
            }
        }
        
        composeTestRule.onNodeWithTag("nextButton")
            .performClick()
        
        val testAnswer = "Sample answer text"
        
        composeTestRule.onNodeWithTag("textAnswerField")
            .performTextInput(testAnswer)
        
        composeTestRule.onNodeWithTag("textAnswerField")
            .assertTextContains(testAnswer)
    }
    
    @Test
    fun testSessionScreen_backButton_navigatesToPreviousQuestion() {
        composeTestRule.setContent {
            DiplomaWorkTheme {
                TestSessionScreen(
                    sessionId = "test_session",
                    onNavigateBack = {},
                    onTestCompleted = { _, _ -> }
                )
            }
        }
        
        composeTestRule.onNodeWithTag("nextButton")
            .performClick()
        
        composeTestRule.onNodeWithTag("previousButton")
            .performClick()
        
        composeTestRule.onNodeWithTag("questionText")
            .assertIsDisplayed()
    }
    
    @Test
    fun testSessionScreen_leaveTestDialog_appearsOnBackPress() {
        composeTestRule.setContent {
            DiplomaWorkTheme {
                TestSessionScreen(
                    sessionId = "test_session",
                    onNavigateBack = {},
                    onTestCompleted = { _, _ -> }
                )
            }
        }
        
        composeTestRule.onNodeWithContentDescription("Navigate back")
            .performClick()
        
        composeTestRule.onNodeWithTag("leaveDialog")
            .assertIsDisplayed()
        
        composeTestRule.onNodeWithTag("cancelButton")
            .assertIsDisplayed()
        
        composeTestRule.onNodeWithTag("leaveButton")
            .assertIsDisplayed()
    }
}
