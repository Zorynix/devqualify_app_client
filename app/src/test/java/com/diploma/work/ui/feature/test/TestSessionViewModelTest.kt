package com.diploma.work.ui.feature.test

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.diploma.work.data.events.ProfileEventBus
import com.diploma.work.data.models.*
import com.diploma.work.data.repository.TestsRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.Assert.*

@OptIn(ExperimentalCoroutinesApi::class)
class TestSessionViewModelTest {
    
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
    fun `initial state is loading`() {
        assertTrue(viewModel.uiState.value.isLoading)
        assertNull(viewModel.uiState.value.testSession)
        assertNull(viewModel.uiState.value.error)
    }
    
    @Test
    fun `loadTestSession with valid id loads session successfully`() = runTest {
        val sessionId = "session123"
        val testSession = TestSession(
            sessionId = sessionId,
            testId = 1L,
            questions = listOf(
                Question(
                    id = 1L,
                    text = "Test Question",
                    type = QuestionType.MULTIPLE_CHOICE,
                    options = listOf("Option 1", "Option 2", "Option 3"),
                    correctOptions = listOf(0),
                    sampleCode = null,
                    points = 1,
                    explanation = "Test explanation"
                )
            ),
            startedAt = System.currentTimeMillis()
        )
        
        coEvery { testsRepository.getTestSession(sessionId) } returns flowOf(Result.success(testSession))
        coEvery { testsRepository.getSessionProgress(sessionId) } returns 0
        
        viewModel.loadTestSession(sessionId)
        
        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(testSession, state.testSession)
        assertNull(state.error)
        assertEquals(0, state.currentQuestionIndex)
    }
    
    @Test
    fun `loadTestSession with invalid id shows error`() = runTest {
        val sessionId = "invalid_session"
        val errorMessage = "Session not found"
        
        coEvery { testsRepository.getTestSession(sessionId) } returns flowOf(Result.failure(Exception(errorMessage)))
        
        viewModel.loadTestSession(sessionId)
        
        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertNull(state.testSession)
        assertEquals(errorMessage, state.error)
    }
    
    @Test
    fun `selectOption updates selected options`() = runTest {
        val option = 1
        
        viewModel.selectOption(option)
        
        val state = viewModel.uiState.value
        assertTrue(state.selectedOptions.contains(option))
    }
    
    @Test
    fun `selectOption with multiple choice allows multiple selections`() = runTest {
        viewModel.selectOption(0)
        viewModel.selectOption(1)
        
        val state = viewModel.uiState.value
        assertTrue(state.selectedOptions.contains(0))
        assertTrue(state.selectedOptions.contains(1))
        assertEquals(2, state.selectedOptions.size)
    }
    
    @Test
    fun `setTextAnswer updates text answer`() = runTest {
        val answer = "Test answer"
        
        viewModel.setTextAnswer(answer)
        
        val state = viewModel.uiState.value
        assertEquals(answer, state.textAnswer)
    }
    
    @Test
    fun `setCodeAnswer updates code answer`() = runTest {
        val code = "println(\"Hello World\")"
        
        viewModel.setCodeAnswer(code)
        
        val state = viewModel.uiState.value
        assertEquals(code, state.codeAnswer)
    }
    
    @Test
    fun `nextQuestion advances to next question`() = runTest {
        val sessionId = "session123"
        val testSession = TestSession(
            sessionId = sessionId,
            testId = 1L,
            questions = listOf(
                Question(
                    id = 1L,
                    text = "Question 1",
                    type = QuestionType.MULTIPLE_CHOICE,
                    options = listOf("Option 1", "Option 2"),
                    correctOptions = listOf(0),
                    sampleCode = null,
                    points = 1,
                    explanation = "Explanation 1"
                ),
                Question(
                    id = 2L,
                    text = "Question 2",
                    type = QuestionType.MULTIPLE_CHOICE,
                    options = listOf("Option 1", "Option 2"),
                    correctOptions = listOf(1),
                    sampleCode = null,
                    points = 1,
                    explanation = "Explanation 2"
                )
            ),
            startedAt = System.currentTimeMillis()
        )
        
        coEvery { testsRepository.getTestSession(sessionId) } returns flowOf(Result.success(testSession))
        coEvery { testsRepository.getSessionProgress(sessionId) } returns 0
        
        viewModel.loadTestSession(sessionId)
        viewModel.nextQuestion()
        
        val state = viewModel.uiState.value
        assertEquals(1, state.currentQuestionIndex)
        assertTrue(state.selectedOptions.isEmpty())
        assertTrue(state.textAnswer.isEmpty())
        assertTrue(state.codeAnswer.isEmpty())
    }
    
    @Test
    fun `previousQuestion goes back to previous question`() = runTest {
        val sessionId = "session123"
        val testSession = TestSession(
            sessionId = sessionId,
            testId = 1L,
            questions = listOf(
                Question(
                    id = 1L,
                    text = "Question 1",
                    type = QuestionType.MULTIPLE_CHOICE,
                    options = listOf("Option 1", "Option 2"),
                    correctOptions = listOf(0),
                    sampleCode = null,
                    points = 1,
                    explanation = "Explanation 1"
                ),
                Question(
                    id = 2L,
                    text = "Question 2",
                    type = QuestionType.MULTIPLE_CHOICE,
                    options = listOf("Option 1", "Option 2"),
                    correctOptions = listOf(1),
                    sampleCode = null,
                    points = 1,
                    explanation = "Explanation 2"
                )
            ),
            startedAt = System.currentTimeMillis()
        )
        
        coEvery { testsRepository.getTestSession(sessionId) } returns flowOf(Result.success(testSession))
        coEvery { testsRepository.getSessionProgress(sessionId) } returns 1
        
        viewModel.loadTestSession(sessionId)
        viewModel.previousQuestion()
        
        val state = viewModel.uiState.value
        assertEquals(0, state.currentQuestionIndex)
    }
    
    @Test
    fun `submitAnswer saves answer successfully`() = runTest {
        val sessionId = "session123"
        val questionId = 1L
        val testSession = TestSession(
            sessionId = sessionId,
            testId = 1L,
            questions = listOf(
                Question(
                    id = questionId,
                    text = "Test Question",
                    type = QuestionType.MULTIPLE_CHOICE,
                    options = listOf("Option 1", "Option 2"),
                    correctOptions = listOf(0),
                    sampleCode = null,
                    points = 1,
                    explanation = "Test explanation"
                )
            ),
            startedAt = System.currentTimeMillis()
        )
        
        coEvery { testsRepository.getTestSession(sessionId) } returns flowOf(Result.success(testSession))
        coEvery { testsRepository.getSessionProgress(sessionId) } returns 0
        coEvery { testsRepository.saveAnswer(sessionId, any()) } returns flowOf(Result.success(true))
        
        viewModel.loadTestSession(sessionId)
        viewModel.selectOption(0)
        viewModel.submitAnswer()
        
        val expectedAnswer = Answer(
            questionId = questionId,
            selectedOptions = listOf(0),
            textAnswer = null,
            codeAnswer = null
        )
        
        coVerify { testsRepository.saveAnswer(sessionId, expectedAnswer) }
    }
    
    @Test
    fun `submitAnswer with incorrect answer shows explanation`() = runTest {
        val sessionId = "session123"
        val questionId = 1L
        val testSession = TestSession(
            sessionId = sessionId,
            testId = 1L,
            questions = listOf(
                Question(
                    id = questionId,
                    text = "Test Question",
                    type = QuestionType.MULTIPLE_CHOICE,
                    options = listOf("Option 1", "Option 2"),
                    correctOptions = listOf(0),
                    sampleCode = null,
                    points = 1,
                    explanation = "Test explanation"
                )
            ),
            startedAt = System.currentTimeMillis()
        )
        
        coEvery { testsRepository.getTestSession(sessionId) } returns flowOf(Result.success(testSession))
        coEvery { testsRepository.getSessionProgress(sessionId) } returns 0
        coEvery { testsRepository.saveAnswer(sessionId, any()) } returns flowOf(Result.success(true))
        
        viewModel.loadTestSession(sessionId)
        viewModel.selectOption(1)
        viewModel.submitAnswer()
        
        val state = viewModel.uiState.value
        assertTrue(state.showExplanation)
        assertEquals("Test explanation", state.explanationText)
        assertTrue(state.incorrectlyAnsweredQuestions.contains(questionId))
    }
    
    @Test
    fun `completeTest submits final results`() = runTest {
        val sessionId = "session123"
        val testResult = TestResult(
            score = 80,
            totalPoints = 100,
            feedback = "Good job!",
            questionResults = emptyList()
        )
        
        coEvery { testsRepository.completeTestSession(sessionId, any()) } returns flowOf(Result.success(testResult))
        
        viewModel.completeTest()
        
        val state = viewModel.uiState.value
        assertTrue(state.testCompleted)
        assertEquals(testResult, state.testResult)
        
        coVerify { testsRepository.completeTestSession(sessionId, any()) }
    }
    
    @Test
    fun `hideExplanation hides explanation dialog`() = runTest {
        viewModel.hideExplanation()
        
        val state = viewModel.uiState.value
        assertFalse(state.showExplanation)
        assertTrue(state.explanationText.isEmpty())
    }
}
