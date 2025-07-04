package com.diploma.work.integration

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.diploma.work.data.AppSession
import com.diploma.work.data.models.*
import com.diploma.work.data.repository.TestsRepository
import com.diploma.work.ui.feature.test.TestSessionViewModel
import com.diploma.work.data.events.ProfileEventBus
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.Assert.*

@RunWith(AndroidJUnit4::class)
@OptIn(ExperimentalCoroutinesApi::class)
class TestFlowIntegrationTest {
    
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()
    
    private val testDispatcher = TestCoroutineDispatcher()
    
    private lateinit var testsRepository: TestsRepository
    private lateinit var profileEventBus: ProfileEventBus
    private lateinit var viewModel: TestSessionViewModel
    
    @Before
    fun setup() {
        testsRepository = mockk()
        profileEventBus = mockk(relaxed = true)
        viewModel = TestSessionViewModel(testsRepository, profileEventBus)
    }
    
    @Test
    fun `complete test flow from start to finish`() = runTest {
        val sessionId = "integration_test_session"
        
        val question1 = Question(
            id = 1L,
            text = "What is Kotlin?",
            type = QuestionType.MULTIPLE_CHOICE,
            options = listOf("Programming Language", "Framework", "Library", "Database"),
            correctOptions = listOf(0),
            sampleCode = null,
            points = 10,
            explanation = "Kotlin is a programming language"
        )
        
        val question2 = Question(
            id = 2L,
            text = "Explain null safety in Kotlin",
            type = QuestionType.TEXT,
            options = emptyList(),
            correctOptions = emptyList(),
            sampleCode = null,
            points = 15,
            explanation = "Kotlin has built-in null safety features"
        )
        
        val testSession = TestSession(
            sessionId = sessionId,
            testId = 1L,
            questions = listOf(question1, question2),
            startedAt = System.currentTimeMillis(),
            answers = emptyMap()
        )
        
        val testResult = TestResult(
            score = 25,
            totalPoints = 25,
            feedback = "Perfect score!",
            questionResults = listOf(
                QuestionResult(
                    questionId = 1L,
                    isCorrect = true,
                    pointsEarned = 10,
                    feedback = "Correct answer!",
                    correctAnswer = "Programming Language",
                    userAnswer = "Programming Language"
                ),
                QuestionResult(
                    questionId = 2L,
                    isCorrect = true,
                    pointsEarned = 15,
                    feedback = "Great explanation!",
                    correctAnswer = "Kotlin provides null safety...",
                    userAnswer = "Kotlin provides null safety..."
                )
            ),
            durationMillis = 300000L
        )
        
        coEvery { testsRepository.getTestSession(sessionId) } returns flowOf(Result.success(testSession))
        coEvery { testsRepository.getSessionProgress(sessionId) } returns 0
        coEvery { testsRepository.saveAnswer(sessionId, any()) } returns flowOf(Result.success(true))
        coEvery { testsRepository.completeTestSession(sessionId, any()) } returns flowOf(Result.success(testResult))
        
        viewModel.loadTestSession(sessionId)
        
        var state = viewModel.uiState.value
        assertEquals(testSession, state.testSession)
        assertEquals(0, state.currentQuestionIndex)
        assertEquals(question1, state.testSession?.questions?.get(0))
        
        viewModel.selectOption(0)
        state = viewModel.uiState.value
        assertTrue(state.selectedOptions.contains(0))
        
        viewModel.submitAnswer()
        state = viewModel.uiState.value
        assertTrue(state.correctlyAnsweredQuestions.contains(1L))
        
        viewModel.nextQuestion()
        state = viewModel.uiState.value
        assertEquals(1, state.currentQuestionIndex)
        assertEquals(question2, state.testSession?.questions?.get(1))
        assertTrue(state.selectedOptions.isEmpty())
        
        viewModel.setTextAnswer("Kotlin provides null safety through nullable and non-nullable types")
        state = viewModel.uiState.value
        assertEquals("Kotlin provides null safety through nullable and non-nullable types", state.textAnswer)
        
        viewModel.submitAnswer()
        state = viewModel.uiState.value
        assertTrue(state.correctlyAnsweredQuestions.contains(2L))
        
        viewModel.completeTest()
        state = viewModel.uiState.value
        assertTrue(state.testCompleted)
        assertEquals(testResult, state.testResult)
        assertEquals(25, state.testResult?.score)
        assertEquals(25, state.testResult?.totalPoints)
        assertEquals("Perfect score!", state.testResult?.feedback)
    }
    
    @Test
    fun `test flow with incorrect answers shows explanations`() = runTest {
        val sessionId = "incorrect_answer_test"
        
        val question = Question(
            id = 1L,
            text = "What is Java?",
            type = QuestionType.MULTIPLE_CHOICE,
            options = listOf("Programming Language", "Coffee", "Island"),
            correctOptions = listOf(0),
            sampleCode = null,
            points = 10,
            explanation = "Java is a programming language, not just coffee or an island"
        )
        
        val testSession = TestSession(
            sessionId = sessionId,
            testId = 1L,
            questions = listOf(question),
            startedAt = System.currentTimeMillis(),
            answers = emptyMap()
        )
        
        coEvery { testsRepository.getTestSession(sessionId) } returns flowOf(Result.success(testSession))
        coEvery { testsRepository.getSessionProgress(sessionId) } returns 0
        coEvery { testsRepository.saveAnswer(sessionId, any()) } returns flowOf(Result.success(true))
        
        viewModel.loadTestSession(sessionId)
        
        viewModel.selectOption(1)
        viewModel.submitAnswer()
        
        val state = viewModel.uiState.value
        assertTrue(state.showExplanation)
        assertEquals("Java is a programming language, not just coffee or an island", state.explanationText)
        assertTrue(state.incorrectlyAnsweredQuestions.contains(1L))
        assertFalse(state.correctlyAnsweredQuestions.contains(1L))
    }
    
    @Test
    fun `test session handles mixed question types correctly`() = runTest {
        val sessionId = "mixed_questions_test"
        
        val multipleChoiceQuestion = Question(
            id = 1L,
            text = "Select all valid Kotlin types",
            type = QuestionType.MULTIPLE_CHOICE,
            options = listOf("Int", "String", "Boolean", "InvalidType"),
            correctOptions = listOf(0, 1, 2),
            sampleCode = null,
            points = 5,
            explanation = "Int, String, and Boolean are valid Kotlin types"
        )
        
        val textQuestion = Question(
            id = 2L,
            text = "Describe the benefits of immutability",
            type = QuestionType.TEXT,
            options = emptyList(),
            correctOptions = emptyList(),
            sampleCode = null,
            points = 10,
            explanation = "Immutability provides thread safety and predictable behavior"
        )
        
        val codeQuestion = Question(
            id = 3L,
            text = "Write a function that returns the sum of two numbers",
            type = QuestionType.CODE,
            options = emptyList(),
            correctOptions = emptyList(),
            sampleCode = "fun sum(a: Int, b: Int): Int {",
            points = 15,
            explanation = "Simple addition function implementation"
        )
        
        val testSession = TestSession(
            sessionId = sessionId,
            testId = 1L,
            questions = listOf(multipleChoiceQuestion, textQuestion, codeQuestion),
            startedAt = System.currentTimeMillis(),
            answers = emptyMap()
        )
        
        coEvery { testsRepository.getTestSession(sessionId) } returns flowOf(Result.success(testSession))
        coEvery { testsRepository.getSessionProgress(sessionId) } returns 0
        coEvery { testsRepository.saveAnswer(sessionId, any()) } returns flowOf(Result.success(true))
        
        viewModel.loadTestSession(sessionId)
        
        var state = viewModel.uiState.value
        assertEquals(QuestionType.MULTIPLE_CHOICE, state.testSession?.questions?.get(0)?.type)
        
        viewModel.selectOption(0)
        viewModel.selectOption(1)
        viewModel.selectOption(2)
        
        state = viewModel.uiState.value
        assertEquals(setOf(0, 1, 2), state.selectedOptions.toSet())
        
        viewModel.nextQuestion()
        
        state = viewModel.uiState.value
        assertEquals(1, state.currentQuestionIndex)
        assertEquals(QuestionType.TEXT, state.testSession?.questions?.get(1)?.type)
        assertTrue(state.selectedOptions.isEmpty())
        
        viewModel.setTextAnswer("Immutability prevents data races and makes code more predictable")
        
        state = viewModel.uiState.value
        assertEquals("Immutability prevents data races and makes code more predictable", state.textAnswer)
        
        viewModel.nextQuestion()
        
        state = viewModel.uiState.value
        assertEquals(2, state.currentQuestionIndex)
        assertEquals(QuestionType.CODE, state.testSession?.questions?.get(2)?.type)
        assertTrue(state.textAnswer.isEmpty())
        
        viewModel.setCodeAnswer("return a + b\n}")
        
        state = viewModel.uiState.value
        assertEquals("return a + b\n}", state.codeAnswer)
    }
}
