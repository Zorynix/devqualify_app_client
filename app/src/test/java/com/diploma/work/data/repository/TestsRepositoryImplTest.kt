package com.diploma.work.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.diploma.work.data.grpc.TestsGrpcClient
import com.diploma.work.data.models.*
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*
import com.diploma.work.utils.Constants

class TestsRepositoryImplTest {
    
    private lateinit var testsGrpcClient: TestsGrpcClient
    private lateinit var context: Context
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor
    private lateinit var testsRepository: TestsRepository
    
    @Before
    fun setup() {
        testsGrpcClient = mockk()
        context = mockk()
        sharedPreferences = mockk()
        editor = mockk(relaxed = true)
        
        every { context.getSharedPreferences(Constants.PrefsKeys.TEST_SESSIONS_PREFS, Context.MODE_PRIVATE) } returns sharedPreferences
        every { sharedPreferences.edit() } returns editor
        every { sharedPreferences.getStringSet(any(), any()) } returns mutableSetOf()
        every { sharedPreferences.getInt(any(), any()) } returns 0
        every { sharedPreferences.getLong(any(), any()) } returns 0L
        every { sharedPreferences.getBoolean(any(), any()) } returns false
        every { sharedPreferences.contains(any()) } returns false
        every { editor.putStringSet(any(), any()) } returns editor
        every { editor.putInt(any(), any()) } returns editor
        every { editor.putLong(any(), any()) } returns editor
        every { editor.remove(any()) } returns editor
        every { editor.apply() } returns Unit
        
        testsRepository = TestsRepositoryImpl(testsGrpcClient, context)
    }
    
    @Test
    fun `getTechnologies returns list of technologies`() = runTest {
        val technologies = listOf(
            Technology(
                id = 1L,
                name = "Kotlin",
                description = "Kotlin programming language",
                direction = Direction.BACKEND
            ),
            Technology(
                id = 2L,
                name = "React",
                description = "React frontend library",
                direction = Direction.FRONTEND
            )
        )
        
        coEvery { testsGrpcClient.getTechnologies(null) } returns flowOf(Result.success(technologies))
        
        val result = testsRepository.getTechnologies().toList()
        
        assertEquals(1, result.size)
        assertTrue(result[0].isSuccess)
        assertEquals(technologies, result[0].getOrNull())
        coVerify { testsGrpcClient.getTechnologies(null) }
    }
    
    @Test
    fun `getTechnologies with direction filter returns filtered technologies`() = runTest {
        val technologies = listOf(
            Technology(
                id = 1L,
                name = "Kotlin",
                description = "Kotlin programming language",
                direction = Direction.BACKEND
            )
        )
        
        coEvery { testsGrpcClient.getTechnologies(Direction.BACKEND) } returns flowOf(Result.success(technologies))
        
        val result = testsRepository.getTechnologies(Direction.BACKEND).toList()
        
        assertEquals(1, result.size)
        assertTrue(result[0].isSuccess)
        assertEquals(technologies, result[0].getOrNull())
        coVerify { testsGrpcClient.getTechnologies(Direction.BACKEND) }
    }
    
    @Test
    fun `getTests returns list of tests`() = runTest {
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
        
        coEvery { testsGrpcClient.getTests(null, null, false) } returns flowOf(Result.success(tests))
        
        val result = testsRepository.getTests().toList()
        
        assertEquals(1, result.size)
        assertTrue(result[0].isSuccess)
        assertEquals(tests, result[0].getOrNull())
        coVerify { testsGrpcClient.getTests(null, null, false) }
    }
    
    @Test
    fun `getTests with filters returns filtered tests`() = runTest {
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
        
        coEvery { testsGrpcClient.getTests(Direction.BACKEND, Level.JUNIOR, true) } returns flowOf(Result.success(tests))
        
        val result = testsRepository.getTests(Direction.BACKEND, Level.JUNIOR, true).toList()
        
        assertEquals(1, result.size)
        assertTrue(result[0].isSuccess)
        assertEquals(tests, result[0].getOrNull())
        coVerify { testsGrpcClient.getTests(Direction.BACKEND, Level.JUNIOR, true) }
    }
    
    @Test
    fun `getTest returns test details`() = runTest {
        val testInfo = TestInfo(
            id = 1L,
            title = "Kotlin Basics",
            description = "Basic Kotlin test",
            direction = Direction.BACKEND,
            level = Level.JUNIOR,
            technologyId = 1L,
            technologyName = "Kotlin",
            isPublished = true,
            questionsCount = 1
        )
        val question = Question(
            id = 1L,
            text = "What is Kotlin?",
            type = QuestionType.MULTIPLE_CHOICE,
            options = listOf("Language", "Framework", "Library"),
            correctOptions = listOf(0),
            sampleCode = null,
            points = 1,
            explanation = "Kotlin is a programming language"
        )
        val test = Test(
            info = testInfo,
            questions = listOf(question)
        )
        
        coEvery { testsGrpcClient.getTest(1L) } returns flowOf(Result.success(test))
        
        val result = testsRepository.getTest(1L).toList()
        
        assertEquals(1, result.size)
        assertTrue(result[0].isSuccess)
        assertEquals(test, result[0].getOrNull())
        coVerify { testsGrpcClient.getTest(1L) }
    }
    
    @Test
    fun `startTestSession creates new session`() = runTest {
        val testSession = TestSession(
            sessionId = "session123",
            testId = 1L,
            questions = emptyList(),
            startedAt = System.currentTimeMillis()
        )
        
        coEvery { testsGrpcClient.startTestSession(1L) } returns flowOf(Result.success(testSession))
        
        val result = testsRepository.startTestSession(1L).toList()
        
        assertEquals(1, result.size)
        assertTrue(result[0].isSuccess)
        assertEquals(testSession, result[0].getOrNull())
        coVerify { testsGrpcClient.startTestSession(1L) }
    }
    
    @Test
    fun `getTestSession returns existing session`() = runTest {
        val sessionId = "session123"
        val testSession = TestSession(
            sessionId = sessionId,
            testId = 1L,
            questions = emptyList(),
            startedAt = System.currentTimeMillis()
        )
        
        coEvery { testsGrpcClient.getTestSession(sessionId) } returns flowOf(Result.success(testSession))
        
        val result = testsRepository.getTestSession(sessionId).toList()
        
        assertEquals(1, result.size)
        assertTrue(result[0].isSuccess)
        assertEquals(testSession, result[0].getOrNull())
        coVerify { testsGrpcClient.getTestSession(sessionId) }
    }
    
    @Test
    fun `saveAnswer saves answer successfully`() = runTest {
        val sessionId = "session123"
        val answer = Answer(
            questionId = 1L,
            selectedOptions = listOf(0),
            textAnswer = null,
            codeAnswer = null
        )
        
        coEvery { testsGrpcClient.saveAnswer(sessionId, answer) } returns flowOf(Result.success(true))
        
        val result = testsRepository.saveAnswer(sessionId, answer).toList()
        
        assertEquals(1, result.size)
        assertTrue(result[0].isSuccess)
        assertTrue(result[0].getOrNull() == true)
        coVerify { testsGrpcClient.saveAnswer(sessionId, answer) }
    }
    
    @Test
    fun `completeTestSession completes session and removes from incomplete sessions`() = runTest {
        val sessionId = "session123"
        val elapsedTime = 60000L
        val testResult = TestResult(
            score = 80,
            totalPoints = 100,
            feedback = "Good job!",
            questionResults = emptyList()
        )
        
        every { sharedPreferences.getStringSet("incomplete_sessions", any()) } returns mutableSetOf(sessionId)
        coEvery { testsGrpcClient.completeTestSession(sessionId, elapsedTime) } returns flowOf(Result.success(testResult))
        
        val result = testsRepository.completeTestSession(sessionId, elapsedTime).toList()
        
        assertEquals(1, result.size)
        assertTrue(result[0].isSuccess)
        assertEquals(testResult, result[0].getOrNull())
        
        verify { editor.putStringSet("incomplete_sessions", emptySet()) }
        verify { editor.remove("progress_$sessionId") }
        verify { editor.remove("timestamp_$sessionId") }
        verify { editor.apply() }
        coVerify { testsGrpcClient.completeTestSession(sessionId, elapsedTime) }
    }
    
    @Test
    fun `getTestResults returns test results`() = runTest {
        val sessionId = "session123"
        val testResult = TestResult(
            score = 80,
            totalPoints = 100,
            feedback = "Good job!",
            questionResults = listOf(
                QuestionResult(
                    questionId = 1L,
                    isCorrect = true,
                    pointsEarned = 1,
                    feedback = "Correct!",
                    correctAnswer = "Language",
                    userAnswer = "Language"
                )
            )
        )
        
        coEvery { testsGrpcClient.getTestResults(sessionId) } returns flowOf(Result.success(testResult))
        
        val result = testsRepository.getTestResults(sessionId).toList()
        
        assertEquals(1, result.size)
        assertTrue(result[0].isSuccess)
        assertEquals(testResult, result[0].getOrNull())
        coVerify { testsGrpcClient.getTestResults(sessionId) }
    }
    
    @Test
    fun `saveSessionProgress saves progress correctly`() = runTest {
        val sessionId = "session123"
        val questionIndex = 5
        val elapsedTime = 30000L
        
        testsRepository.saveSessionProgress(sessionId, questionIndex, elapsedTime)
        
        verify { editor.putInt("progress_$sessionId", questionIndex) }
        verify { editor.putLong("timestamp_$sessionId", any()) }
        verify { editor.putLong("elapsed_time_$sessionId", elapsedTime) }
        verify { editor.apply() }
    }
    
    @Test
    fun `getSessionProgress returns saved progress`() = runTest {
        val sessionId = "session123"
        val expectedProgress = 5
        
        every { sharedPreferences.getInt("progress_$sessionId", -1) } returns expectedProgress
        
        val result = testsRepository.getSessionProgress(sessionId)
        
        assertEquals(expectedProgress, result)
    }
    
    @Test
    fun `getSessionProgress returns null when no progress saved`() = runTest {
        val sessionId = "session123"
        
        every { sharedPreferences.getInt("progress_$sessionId", -1) } returns -1
        
        val result = testsRepository.getSessionProgress(sessionId)
        
        assertNull(result)
    }
    
    @Test
    fun `getSessionElapsedTime returns saved elapsed time`() = runTest {
        val sessionId = "session123"
        val expectedElapsedTime = 30000L
        
        every { sharedPreferences.getLong("elapsed_time_$sessionId", -1) } returns expectedElapsedTime
        
        val result = testsRepository.getSessionElapsedTime(sessionId)
        
        assertEquals(expectedElapsedTime, result)
    }
    
    @Test
    fun `getSessionElapsedTime returns null when no elapsed time saved`() = runTest {
        val sessionId = "session123"
        
        every { sharedPreferences.getLong("elapsed_time_$sessionId", -1) } returns -1L
        
        val result = testsRepository.getSessionElapsedTime(sessionId)
        
        assertNull(result)
    }
    
    @Test
    fun `getUncompletedSessions returns list of uncompleted sessions`() = runTest {
        val sessionId1 = "session123"
        val sessionId2 = "session456"
        val sessionIds = setOf(sessionId1, sessionId2)
        
        val testSession1 = TestSession(
            sessionId = sessionId1,
            testId = 1L,
            questions = emptyList(),
            startedAt = System.currentTimeMillis()
        )
        val testSession2 = TestSession(
            sessionId = sessionId2,
            testId = 2L,
            questions = emptyList(),
            startedAt = System.currentTimeMillis()
        )
        
        every { sharedPreferences.getStringSet("incomplete_sessions", emptySet()) } returns sessionIds
        coEvery { testsGrpcClient.getTestSession(sessionId1) } returns flowOf(Result.success(testSession1))
        coEvery { testsGrpcClient.getTestSession(sessionId2) } returns flowOf(Result.success(testSession2))
        
        val result = testsRepository.getUncompletedSessions()
        
        assertEquals(2, result.size)
        assertTrue(result.any { it.sessionId == sessionId1 })
        assertTrue(result.any { it.sessionId == sessionId2 })
    }
    
    @Test
    fun `removeUncompletedSession removes session from incomplete sessions`() = runTest {
        val sessionId = "session123"
        val sessionIds = mutableSetOf(sessionId, "session456")
        
        every { sharedPreferences.getStringSet("incomplete_sessions", any()) } returns sessionIds
        
        testsRepository.removeUncompletedSession(sessionId)
        
        verify { editor.putStringSet("incomplete_sessions", setOf("session456")) }
        verify { editor.remove("progress_$sessionId") }
        verify { editor.remove("timestamp_$sessionId") }
        verify { editor.apply() }
    }
}
