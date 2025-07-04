package com.diploma.work.ui.feature.test

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.diploma.work.data.models.*
import com.diploma.work.ui.theme.DiplomaWorkTheme
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TestSessionScreenTest {
    
    @get:Rule
    val composeTestRule = createComposeRule()
    
    private lateinit var testSession: TestSession
    private lateinit var multipleChoiceQuestion: Question
    private lateinit var textQuestion: Question
    
    @Before
    fun setup() {
        multipleChoiceQuestion = Question(
            id = 1L,
            text = "What is Kotlin?",
            type = QuestionType.MULTIPLE_CHOICE,
            options = listOf("Programming Language", "Framework", "Library", "Database"),
            correctOptions = listOf(0),
            sampleCode = null,
            points = 1,
            explanation = "Kotlin is a programming language"
        )
        
        textQuestion = Question(
            id = 2L,
            text = "Explain the benefits of Kotlin",
            type = QuestionType.TEXT,
            options = emptyList(),
            correctOptions = emptyList(),
            sampleCode = null,
            points = 2,
            explanation = "Various benefits include null safety, interoperability, etc."
        )
        
        testSession = TestSession(
            sessionId = "test_session",
            testId = 1L,
            questions = listOf(multipleChoiceQuestion, textQuestion),
            startedAt = System.currentTimeMillis()
        )
    }
    
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
        
        composeTestRule.onNodeWithText("What is Kotlin?")
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
        
        composeTestRule.onNodeWithText("Programming Language")
            .assertIsDisplayed()
        composeTestRule.onNodeWithText("Framework")
            .assertIsDisplayed()
        composeTestRule.onNodeWithText("Library")
            .assertIsDisplayed()
        composeTestRule.onNodeWithText("Database")
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
        
        composeTestRule.onNodeWithText("Programming Language")
            .performClick()
        
        composeTestRule.onNodeWithText("Programming Language")
            .assertIsSelected()
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
    fun testSessionScreen_submitButton_appearsOnLastQuestion() {
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
        
        composeTestRule.onNodeWithText("Submit Test")
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
        
        composeTestRule.onNodeWithText("1 of 2")
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
        
        composeTestRule.onNodeWithText("Explain the benefits of Kotlin")
            .assertIsDisplayed()
        
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
        
        val testAnswer = "Kotlin provides null safety and is interoperable with Java"
        
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
        
        composeTestRule.onNodeWithText("Explain the benefits of Kotlin")
            .assertIsDisplayed()
        
        composeTestRule.onNodeWithTag("previousButton")
            .performClick()
        
        composeTestRule.onNodeWithText("What is Kotlin?")
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
        
        composeTestRule.onNodeWithText("Leave Test?")
            .assertIsDisplayed()
        
        composeTestRule.onNodeWithText("Your progress will be saved.")
            .assertIsDisplayed()
        
        composeTestRule.onNodeWithText("Cancel")
            .assertIsDisplayed()
        
        composeTestRule.onNodeWithText("Leave")
            .assertIsDisplayed()
    }
}
