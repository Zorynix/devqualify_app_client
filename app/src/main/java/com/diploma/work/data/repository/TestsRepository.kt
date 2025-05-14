package com.diploma.work.data.repository

import com.diploma.work.data.models.*
import kotlinx.coroutines.flow.Flow
import com.diploma.work.data.models.Direction
import com.diploma.work.data.models.Level

interface TestsRepository {
    fun getTechnologies(direction: Direction? = null): Flow<Result<List<Technology>>>

    fun getTests(
        direction: Direction? = null,
        level: Level? = null,
        includeUnpublished: Boolean = false
    ): Flow<Result<List<TestInfo>>>

    fun getTestsByTechnology(
        technologyId: Long,
        level: Level? = null,
        includeUnpublished: Boolean = false
    ): Flow<Result<List<TestInfo>>>

    fun getTest(testId: Long): Flow<Result<Test>>

    fun startTestSession(testId: Long): Flow<Result<TestSession>>

    fun getTestSession(sessionId: String): Flow<Result<TestSession>>

    fun saveAnswer(
        sessionId: String,
        answer: Answer
    ): Flow<Result<Boolean>>

    fun completeTestSession(sessionId: String): Flow<Result<TestResult>>

    fun getTestResults(sessionId: String): Flow<Result<TestResult>>
    
    suspend fun saveSessionProgress(sessionId: String, questionIndex: Int, elapsedTimeMillis: Long = 0)
    
    suspend fun getSessionProgress(sessionId: String): Int?
    
    suspend fun getUncompletedSessions(): List<TestSession>
    
    suspend fun removeUncompletedSession(sessionId: String)

    suspend fun getSessionElapsedTime(sessionId: String): Long?
}
