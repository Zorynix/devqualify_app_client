package com.diploma.work.ui.feature.home

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.diploma.work.data.models.*
import com.diploma.work.data.repository.TestsRepository
import com.diploma.work.utils.ErrorHandler
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
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
class HomeViewModelTest {
    
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()
    
    private val testDispatcher: TestDispatcher = StandardTestDispatcher()
    
    private lateinit var testsRepository: TestsRepository
    private lateinit var errorHandler: ErrorHandler
    private lateinit var viewModel: HomeViewModel
    
    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        testsRepository = mockk()
        errorHandler = mockk(relaxed = true)
        
        every { testsRepository.getTests(any(), any(), any()) } returns flowOf(Result.success(emptyList()))
        every { testsRepository.getTechnologies(any()) } returns flowOf(Result.success(emptyList()))
        
        viewModel = HomeViewModel(testsRepository, errorHandler)
    }
    
    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }
    
    @Test
    fun `initial state is loading`() = runTest {
        assertTrue(viewModel.isLoading.value)
        
        advanceUntilIdle()
        
        assertFalse(viewModel.isLoading.value)
        assertTrue(viewModel.uiState.value.tests.isEmpty())
        assertTrue(viewModel.uiState.value.technologies.isEmpty())
        assertNull(viewModel.uiState.value.selectedTechnology)
        assertNull(viewModel.uiState.value.selectedDirection)
        assertNull(viewModel.uiState.value.selectedLevel)
        assertNull(viewModel.uiState.value.error)
    }
    
    @Test
    fun `loadTests loads tests successfully`() = runTest {
        val tests = listOf(
            TestInfo(
                id = 1L,
                title = "Kotlin Basics",
                description = "Basic Kotlin test",
                direction = Direction.BACKEND,
                level = Level.JUNIOR,
                technologyId = 1L,
                technologyName = "Kotlin",
                isPublished = true,
                questionsCount = 10
            ),
            TestInfo(
                id = 2L,
                title = "React Fundamentals",
                description = "Basic React test",
                direction = Direction.FRONTEND,
                level = Level.JUNIOR,
                technologyId = 2L,
                technologyName = "React",
                isPublished = true,
                questionsCount = 15
            )
        )
        
        every { testsRepository.getTests(null, null, false) } returns flowOf(Result.success(tests))
        every { testsRepository.getTechnologies() } returns flowOf(Result.success(emptyList()))
        
        viewModel = HomeViewModel(testsRepository, errorHandler)
        advanceUntilIdle()
        
        viewModel.loadTests()
        advanceUntilIdle()
        
        val state = viewModel.uiState.value
        assertFalse(viewModel.isLoading.value)
        assertEquals(tests, state.tests)
        assertNull(state.error)
    }
    
    @Test
    fun `loadTests with direction filter loads filtered tests`() = runTest {
        val tests = listOf(
            TestInfo(
                id = 1L,
                title = "Kotlin Basics",
                description = "Basic Kotlin test",
                direction = Direction.BACKEND,
                level = Level.JUNIOR,
                technologyId = 1L,
                technologyName = "Kotlin",
                isPublished = true,
                questionsCount = 10
            )
        )
        
        every { testsRepository.getTests(Direction.BACKEND, null, false) } returns flowOf(Result.success(tests))
        every { testsRepository.getTechnologies() } returns flowOf(Result.success(emptyList()))
        
        viewModel.selectDirection(Direction.BACKEND)
        advanceUntilIdle()
        
        val state = viewModel.uiState.value
        assertFalse(viewModel.isLoading.value)
        assertEquals(tests, state.tests)
        assertEquals(Direction.BACKEND, state.selectedDirection)
        assertNull(state.error)
    }
    
    @Test
    fun `loadTests with level filter loads filtered tests`() = runTest {
        val tests = listOf(
            TestInfo(
                id = 1L,
                title = "Kotlin Advanced",
                description = "Advanced Kotlin test",
                direction = Direction.BACKEND,
                level = Level.SENIOR,
                technologyId = 1L,
                technologyName = "Kotlin",
                isPublished = true,
                questionsCount = 20
            )
        )
        
        every { testsRepository.getTests(null, Level.SENIOR, false) } returns flowOf(Result.success(tests))
        every { testsRepository.getTechnologies() } returns flowOf(Result.success(emptyList()))
        
        viewModel.selectLevel(Level.SENIOR)
        advanceUntilIdle()
        
        val state = viewModel.uiState.value
        assertFalse(viewModel.isLoading.value)
        assertEquals(tests, state.tests)
        assertEquals(Level.SENIOR, state.selectedLevel)
        assertNull(state.error)
    }
    
    @Test
    fun `loadTests with technology filter loads tests by technology`() = runTest {
        val technology = Technology(
            id = 1L,
            name = "Kotlin",
            description = "Kotlin programming language",
            direction = Direction.BACKEND
        )
        val tests = listOf(
            TestInfo(
                id = 1L,
                title = "Kotlin Basics",
                description = "Basic Kotlin test",
                direction = Direction.BACKEND,
                level = Level.JUNIOR,
                technologyId = 1L,
                technologyName = "Kotlin",
                isPublished = true,
                questionsCount = 10
            )
        )
        
        every { testsRepository.getTestsByTechnology(1L, null, false) } returns flowOf(Result.success(tests))
        every { testsRepository.getTechnologies() } returns flowOf(Result.success(emptyList()))
        
        viewModel.selectTechnology(technology)
        advanceUntilIdle()
        
        val state = viewModel.uiState.value
        assertFalse(viewModel.isLoading.value)
        assertEquals(tests, state.tests)
        assertEquals(technology, state.selectedTechnology)
        assertNull(state.error)
    }
    
    @Test
    fun `loadTests with combined filters loads filtered tests`() = runTest {
        val technology = Technology(
            id = 1L,
            name = "Kotlin",
            description = "Kotlin programming language",
            direction = Direction.BACKEND
        )
        val tests = listOf(
            TestInfo(
                id = 1L,
                title = "Kotlin Middle",
                description = "Middle Kotlin test",
                direction = Direction.BACKEND,
                level = Level.MIDDLE,
                technologyId = 1L,
                technologyName = "Kotlin",
                isPublished = true,
                questionsCount = 15
            )
        )
        
        every { testsRepository.getTestsByTechnology(1L, Level.MIDDLE, false) } returns flowOf(Result.success(tests))
        every { testsRepository.getTechnologies() } returns flowOf(Result.success(emptyList()))
        
        viewModel.selectTechnology(technology)
        viewModel.selectLevel(Level.MIDDLE)
        advanceUntilIdle()
        
        val state = viewModel.uiState.value
        assertFalse(viewModel.isLoading.value)
        assertEquals(tests, state.tests)
        assertEquals(technology, state.selectedTechnology)
        assertEquals(Level.MIDDLE, state.selectedLevel)
        assertNull(state.error)
    }
    
    @Test
    fun `loadTests with error shows error message`() = runTest {
        val errorMessage = "Failed to load tests"
        
        every { testsRepository.getTests(null, null, false) } returns flowOf(Result.failure(Exception(errorMessage)))
        every { testsRepository.getTechnologies() } returns flowOf(Result.success(emptyList()))
        
        viewModel.loadTests()
        advanceUntilIdle()
        
        val state = viewModel.uiState.value
        assertFalse(viewModel.isLoading.value)
        assertTrue(state.tests.isEmpty())
        assertNotNull(viewModel.errorMessage.value)
    }
    
    @Test
    fun `selectDirection updates selected direction and reloads tests`() = runTest {
        val tests = listOf(
            TestInfo(
                id = 1L,
                title = "Frontend Test",
                description = "Frontend test",
                direction = Direction.FRONTEND,
                level = Level.JUNIOR,
                technologyId = 2L,
                technologyName = "React",
                isPublished = true,
                questionsCount = 10
            )
        )
        
        every { testsRepository.getTests(Direction.FRONTEND, null, false) } returns flowOf(Result.success(tests))
        every { testsRepository.getTechnologies() } returns flowOf(Result.success(emptyList()))
        
        viewModel.selectDirection(Direction.FRONTEND)
        advanceUntilIdle()
        
        val state = viewModel.uiState.value
        assertEquals(Direction.FRONTEND, state.selectedDirection)
        assertEquals(tests, state.tests)
    }
    
    @Test
    fun `selectDirection with null clears direction filter`() = runTest {
        val tests = listOf(
            TestInfo(
                id = 1L,
                title = "All Tests",
                description = "Test without direction filter",
                direction = Direction.BACKEND,
                level = Level.JUNIOR,
                technologyId = 1L,
                technologyName = "Kotlin",
                isPublished = true,
                questionsCount = 10
            )
        )
        
        every { testsRepository.getTests(null, null, false) } returns flowOf(Result.success(tests))
        every { testsRepository.getTechnologies() } returns flowOf(Result.success(emptyList()))
        
        viewModel.selectDirection(null)
        advanceUntilIdle()
        
        val state = viewModel.uiState.value
        assertNull(state.selectedDirection)
        assertEquals(tests, state.tests)
    }
    
    @Test
    fun `selectLevel updates selected level and reloads tests`() = runTest {
        val tests = listOf(
            TestInfo(
                id = 1L,
                title = "Senior Test",
                description = "Senior level test",
                direction = Direction.BACKEND,
                level = Level.SENIOR,
                technologyId = 1L,
                technologyName = "Kotlin",
                isPublished = true,
                questionsCount = 25
            )
        )
        
        every { testsRepository.getTests(null, Level.SENIOR, false) } returns flowOf(Result.success(tests))
        every { testsRepository.getTechnologies() } returns flowOf(Result.success(emptyList()))
        
        viewModel.selectLevel(Level.SENIOR)
        advanceUntilIdle()
        
        val state = viewModel.uiState.value
        assertEquals(Level.SENIOR, state.selectedLevel)
        assertEquals(tests, state.tests)
    }
    
    @Test
    fun `selectTechnology updates selected technology and reloads tests`() = runTest {
        val technology = Technology(
            id = 2L,
            name = "React",
            description = "React frontend library",
            direction = Direction.FRONTEND
        )
        val tests = listOf(
            TestInfo(
                id = 1L,
                title = "React Test",
                description = "React test",
                direction = Direction.FRONTEND,
                level = Level.JUNIOR,
                technologyId = 2L,
                technologyName = "React",
                isPublished = true,
                questionsCount = 12
            )
        )
        
        every { testsRepository.getTestsByTechnology(2L, null, false) } returns flowOf(Result.success(tests))
        every { testsRepository.getTechnologies() } returns flowOf(Result.success(emptyList()))
        
        viewModel.selectTechnology(technology)
        advanceUntilIdle()
        
        val state = viewModel.uiState.value
        assertEquals(technology, state.selectedTechnology)
        assertEquals(tests, state.tests)
    }
    
    @Test
    fun `selectTechnology with null clears technology filter`() = runTest {
        val tests = listOf(
            TestInfo(
                id = 1L,
                title = "All Tests",
                description = "Test without technology filter",
                direction = Direction.BACKEND,
                level = Level.JUNIOR,
                technologyId = 1L,
                technologyName = "Kotlin",
                isPublished = true,
                questionsCount = 10
            )
        )
        
        every { testsRepository.getTests(null, null, false) } returns flowOf(Result.success(tests))
        
        viewModel.selectTechnology(null)
        advanceUntilIdle()
        
        val state = viewModel.uiState.value
        assertNull(state.selectedTechnology)
        assertEquals(tests, state.tests)
    }
}






