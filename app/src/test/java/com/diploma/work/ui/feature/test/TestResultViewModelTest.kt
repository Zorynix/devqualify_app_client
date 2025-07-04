package com.diploma.work.ui.feature.test

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.diploma.work.data.models.QuestionResult
import com.diploma.work.data.models.TestResult
import com.diploma.work.data.repository.TestsRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TestResultViewModelTest {
    
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()
    
    private val testDispatcher = TestCoroutineDispatcher()
    
    private lateinit var testsRepository: TestsRepository
    private lateinit var viewModel: TestResultViewModel
    
    @Before
    fun setup() {
        testsRepository = mockk()
        viewModel = TestResultViewModel(testsRepository)
    }
    
    @Test
    fun `initial state is loading`() {
        assertTrue(viewModel.uiState.value.isLoading)
        assertNull(viewModel.uiState.value.result)
        assertNull(viewModel.uiState.value.error)
    }
    
    @Test
    fun `loadTestResult success updates state correctly`() = runTest {
        val sessionId = "test_session_123"
        val testResult = TestResult(
            score = 85,
            totalPoints = 100,
            feedback = "Great performance!",
            questionResults = listOf(
                QuestionResult(
                    questionId = 1L,
                    isCorrect = true,
                    pointsEarned = 10,
                    feedback = "Correct!",
                    correctAnswer = "A",
                    userAnswer = "A"
                )
            ),
            durationMillis = 300000L
        )
        
        coEvery { testsRepository.getTestResults(sessionId) } returns flowOf(Result.success(testResult))
        coEvery { testsRepository.getSessionElapsedTime(sessionId) } returns 300000L
        
        viewModel.loadTestResult(sessionId)
        
        assertFalse(viewModel.uiState.value.isLoading)
        assertNotNull(viewModel.uiState.value.result)
        assertNull(viewModel.uiState.value.error)
        assertEquals(85, viewModel.uiState.value.result?.score)
        assertEquals(100, viewModel.uiState.value.result?.totalPoints)
        assertEquals("Great performance!", viewModel.uiState.value.result?.feedback)
        assertEquals(300000L, viewModel.uiState.value.result?.durationMillis)
        
        coVerify { testsRepository.getTestResults(sessionId) }
        coVerify { testsRepository.getSessionElapsedTime(sessionId) }
    }
    
    @Test
    fun `loadTestResult failure updates error state`() = runTest {
        val sessionId = "test_session_123"
        val errorMessage = "Failed to load test results"
        
        coEvery { testsRepository.getTestResults(sessionId) } returns flowOf(Result.failure(Exception(errorMessage)))
        coEvery { testsRepository.getSessionElapsedTime(sessionId) } returns 0L
        
        viewModel.loadTestResult(sessionId)
        
        assertFalse(viewModel.uiState.value.isLoading)
        assertNull(viewModel.uiState.value.result)
        assertEquals(errorMessage, viewModel.uiState.value.error)
    }
    
    @Test
    fun `loadTestResult with null elapsed time uses default`() = runTest {
        val sessionId = "test_session_123"
        val testResult = TestResult(
            score = 75,
            totalPoints = 100,
            feedback = "Good job!",
            questionResults = emptyList(),
            durationMillis = 0L
        )
        
        coEvery { testsRepository.getTestResults(sessionId) } returns flowOf(Result.success(testResult))
        coEvery { testsRepository.getSessionElapsedTime(sessionId) } returns null
        
        viewModel.loadTestResult(sessionId)
        
        assertFalse(viewModel.uiState.value.isLoading)
        assertNotNull(viewModel.uiState.value.result)
        assertEquals(0L, viewModel.uiState.value.result?.durationMillis)
    }
    
    @Test
    fun `loadTestResult with repository exception handles error`() = runTest {
        val sessionId = "test_session_123"
        
        coEvery { testsRepository.getTestResults(sessionId) } throws RuntimeException("Network error")
        coEvery { testsRepository.getSessionElapsedTime(sessionId) } returns 0L
        
        viewModel.loadTestResult(sessionId)
        
        assertFalse(viewModel.uiState.value.isLoading)
        assertNull(viewModel.uiState.value.result)
        assertNotNull(viewModel.uiState.value.error)
    }
    
    @Test
    fun `loadTestResult with perfect score`() = runTest {
        val sessionId = "perfect_session"
        val testResult = TestResult(
            score = 100,
            totalPoints = 100,
            feedback = "Perfect score!",
            questionResults = listOf(
                QuestionResult(
                    questionId = 1L,
                    isCorrect = true,
                    pointsEarned = 50,
                    feedback = "Excellent!",
                    correctAnswer = "Correct answer",
                    userAnswer = "Correct answer"
                ),
                QuestionResult(
                    questionId = 2L,
                    isCorrect = true,
                    pointsEarned = 50,
                    feedback = "Perfect!",
                    correctAnswer = "Another correct answer",
                    userAnswer = "Another correct answer"
                )
            ),
            durationMillis = 120000L
        )
        
        coEvery { testsRepository.getTestResults(sessionId) } returns flowOf(Result.success(testResult))
        coEvery { testsRepository.getSessionElapsedTime(sessionId) } returns 120000L
        
        viewModel.loadTestResult(sessionId)
        
        assertEquals(100, viewModel.uiState.value.result?.score)
        assertEquals(100, viewModel.uiState.value.result?.totalPoints)
        assertEquals(2, viewModel.uiState.value.result?.questionResults?.size)
        assertTrue(viewModel.uiState.value.result?.questionResults?.all { it.isCorrect } == true)
    }
    
    @Test
    fun `loadTestResult with failing score`() = runTest {
        val sessionId = "failing_session"
        val testResult = TestResult(
            score = 25,
            totalPoints = 100,
            feedback = "Keep practicing!",
            questionResults = listOf(
                QuestionResult(
                    questionId = 1L,
                    isCorrect = false,
                    pointsEarned = 0,
                    feedback = "Incorrect",
                    correctAnswer = "A",
                    userAnswer = "B"
                ),
                QuestionResult(
                    questionId = 2L,
                    isCorrect = true,
                    pointsEarned = 25,
                    feedback = "Good!",
                    correctAnswer = "C",
                    userAnswer = "C"
                )
            ),
            durationMillis = 600000L
        )
        
        coEvery { testsRepository.getTestResults(sessionId) } returns flowOf(Result.success(testResult))
        coEvery { testsRepository.getSessionElapsedTime(sessionId) } returns 600000L
        
        viewModel.loadTestResult(sessionId)
        
        assertEquals(25, viewModel.uiState.value.result?.score)
        assertEquals(100, viewModel.uiState.value.result?.totalPoints)
        assertEquals("Keep practicing!", viewModel.uiState.value.result?.feedback)
        assertEquals(2, viewModel.uiState.value.result?.questionResults?.size)
        assertFalse(viewModel.uiState.value.result?.questionResults?.all { it.isCorrect } == true)
    }
}
