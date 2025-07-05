package com.diploma.work.ui.feature.test

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.diploma.work.data.models.*
import com.diploma.work.data.repository.TestsRepository
import com.diploma.work.utils.ErrorHandler
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.test.resetMain
import org.junit.Before
import org.junit.After
import org.junit.Rule
import org.junit.Test
import org.junit.Assert.*

@OptIn(ExperimentalCoroutinesApi::class)
class TestDetailsViewModelTest {
    
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()
    
    private val testDispatcher: TestDispatcher = StandardTestDispatcher()
    
    private lateinit var testsRepository: TestsRepository
    private lateinit var errorHandler: ErrorHandler
    private lateinit var viewModel: TestDetailsViewModel
    
    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        testsRepository = mockk()
        errorHandler = mockk(relaxed = true)
        viewModel = TestDetailsViewModel(testsRepository, errorHandler)
    }
    
    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }
    
    @Test
    fun `initial state is loading`() = runTest {
        assertTrue(viewModel.uiState.value.isLoading)
        assertNull(viewModel.uiState.value.test)
        assertNull(viewModel.uiState.value.error)
    }
    
    @Test
    fun `loadTest with valid id loads test successfully`() = runTest {
        val testId = 1L
        val testInfo = TestInfo(
            id = testId,
            title = "Test Title",
            description = "Test Description",
            direction = Direction.BACKEND,
            level = Level.JUNIOR,
            technologyId = 1L,
            technologyName = "Kotlin",
            isPublished = true,
            questionsCount = 10
        )
        val question = Question(
            id = 1L,
            text = "Test Question",
            type = QuestionType.MULTIPLE_CHOICE,
            options = listOf("Option 1", "Option 2", "Option 3"),
            correctOptions = listOf(0),
            sampleCode = null,
            points = 1,
            explanation = "Test explanation"
        )
        val test = Test(
            info = testInfo,
            questions = listOf(question)
        )
        
        coEvery { testsRepository.getTest(testId) } returns flowOf(Result.success(test))
        coEvery { testsRepository.getUncompletedSessions() } returns emptyList()
        
        viewModel.loadTest(testId)
        
        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(test, state.test)
        assertNull(state.error)
        assertFalse(state.hasUnfinishedSession)
    }
    
    @Test
    fun `loadTest with invalid id shows error`() = runTest {
        val testId = 999L
        val errorMessage = "Test not found"
        
        coEvery { testsRepository.getTest(testId) } returns flowOf(Result.failure(Exception(errorMessage)))
        coEvery { testsRepository.getUncompletedSessions() } returns emptyList()
        
        viewModel.loadTest(testId)
        
        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertNull(state.test)
        assertEquals(errorMessage, state.error)
    }
    
    @Test
    fun `loadTest with unfinished session shows dialog`() = runTest {
        val testId = 1L
        val sessionId = "session123"
        val testInfo = TestInfo(
            id = testId,
            title = "Test Title",
            description = "Test Description",
            direction = Direction.BACKEND,
            level = Level.JUNIOR,
            technologyId = 1L,
            technologyName = "Kotlin",
            isPublished = true,
            questionsCount = 10
        )
        val test = Test(
            info = testInfo,
            questions = emptyList()
        )
        val unfinishedSession = TestSession(
            sessionId = sessionId,
            testId = testId,
            questions = emptyList(),
            startedAt = System.currentTimeMillis()
        )
        
        coEvery { testsRepository.getTest(testId) } returns flowOf(Result.success(test))
        coEvery { testsRepository.getUncompletedSessions() } returns listOf(unfinishedSession)
        coEvery { testsRepository.getSessionProgress(sessionId) } returns 5
        
        viewModel.loadTest(testId)
        
        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertTrue(state.hasUnfinishedSession)
        assertEquals(sessionId, state.unfinishedSessionId)
        assertEquals(5, state.lastSavedQuestionIndex)
    }
    
    @Test
    fun `startTest creates new session successfully`() = runTest {
        val testId = 1L
        val sessionId = "new_session"
        val testSession = TestSession(
            sessionId = sessionId,
            testId = testId,
            questions = emptyList(),
            startedAt = System.currentTimeMillis()
        )
        
        coEvery { testsRepository.startTestSession(testId) } returns flowOf(Result.success(testSession))
        
        viewModel.startTest()
        
        val state = viewModel.uiState.value
        assertFalse(state.isStartingTest)
        assertEquals(sessionId, state.testSessionId)
        assertNull(state.error)
        
        coVerify { testsRepository.startTestSession(testId) }
    }
    
    @Test
    fun `startTest with error shows error message`() = runTest {
        val testId = 1L
        val errorMessage = "Failed to start test"
        
        coEvery { testsRepository.startTestSession(testId) } returns flowOf(Result.failure(Exception(errorMessage)))
        
        viewModel.startTest()
        
        val state = viewModel.uiState.value
        assertFalse(state.isStartingTest)
        assertNull(state.testSessionId)
        assertEquals(errorMessage, state.error)
    }
    
    @Test
    fun `continueTest with valid session id sets session id`() = runTest {
        val sessionId = "existing_session"
        
        viewModel.continueTest()
        
        val state = viewModel.uiState.value
        assertEquals(sessionId, state.testSessionId)
    }
    
    @Test
    fun `startNewTest removes unfinished session and starts new one`() = runTest {
        val testId = 1L
        val oldSessionId = "old_session"
        val newSessionId = "new_session"
        val testSession = TestSession(
            sessionId = newSessionId,
            testId = testId,
            questions = emptyList(),
            startedAt = System.currentTimeMillis()
        )
        
        coEvery { testsRepository.removeUncompletedSession(oldSessionId) } returns Unit
        coEvery { testsRepository.startTestSession(testId) } returns flowOf(Result.success(testSession))
        
        viewModel.startTest()
        
        val state = viewModel.uiState.value
        assertFalse(state.isStartingTest)
        assertFalse(state.hasUnfinishedSession)
        assertEquals(newSessionId, state.testSessionId)
        
        coVerify { testsRepository.removeUncompletedSession(oldSessionId) }
        coVerify { testsRepository.startTestSession(testId) }
    }
}






