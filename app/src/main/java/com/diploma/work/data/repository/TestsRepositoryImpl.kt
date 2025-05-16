package com.diploma.work.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.diploma.work.data.grpc.TestsGrpcClient
import com.diploma.work.data.models.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import com.diploma.work.data.models.Direction
import com.diploma.work.data.models.Level
import com.orhanobut.logger.Logger
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TestsRepositoryImpl @Inject constructor(
    private val testsGrpcClient: TestsGrpcClient,
    private val context: Context
) : TestsRepository {
    private val tag = "repo.tests"
    
    private val sessionPrefs: SharedPreferences by lazy {
        context.getSharedPreferences("test_sessions_prefs", Context.MODE_PRIVATE) 
    }

    override fun getTechnologies(direction: Direction?): Flow<Result<List<Technology>>> {
        Logger.d("$tag: Getting technologies" + (direction?.let { " for direction: $it" } ?: ""))
        return testsGrpcClient.getTechnologies(direction)
    }

    override fun getTests(
        direction: Direction?,
        level: Level?,
        includeUnpublished: Boolean
    ): Flow<Result<List<TestInfo>>> {
        Logger.d("$tag: Getting tests" +
            (direction?.let { " for direction: $it" } ?: "") +
            (level?.let { ", level: $it" } ?: "") +
            ", includeUnpublished: $includeUnpublished")

        return testsGrpcClient.getTests(direction, level, includeUnpublished)
    }

    override fun getTestsByTechnology(
        technologyId: Long,
        level: Level?,
        includeUnpublished: Boolean
    ): Flow<Result<List<TestInfo>>> {
        Logger.d("$tag: Getting tests for technology ID: $technologyId" +
            (level?.let { ", level: $it" } ?: "") +
            ", includeUnpublished: $includeUnpublished")

        return testsGrpcClient.getTestsByTechnology(technologyId, level, includeUnpublished)
    }

    override fun getTest(testId: Long): Flow<Result<Test>> {
        Logger.d("$tag: Getting test details for test ID: $testId")
        return testsGrpcClient.getTest(testId)
    }

    override fun startTestSession(testId: Long): Flow<Result<TestSession>> {
        Logger.d("$tag: Starting test session for test ID: $testId")
        return testsGrpcClient.startTestSession(testId)
    }

    override fun getTestSession(sessionId: String): Flow<Result<TestSession>> {
        Logger.d("$tag: Getting test session for session ID: $sessionId")
        return testsGrpcClient.getTestSession(sessionId)
    }

    override fun saveAnswer(sessionId: String, answer: Answer): Flow<Result<Boolean>> {
        Logger.d("$tag: Saving answer for session ID: $sessionId, question ID: ${answer.questionId}")
        return testsGrpcClient.saveAnswer(sessionId, answer)
    }

    override fun completeTestSession(sessionId: String): Flow<Result<TestResult>> {
        Logger.d("$tag: Completing test session for session ID: $sessionId")
        val elapsedTime = sessionPrefs.getLong(getElapsedTimeKey(sessionId), 0L)
        Logger.d("$tag: Saving elapsed time before completion: $elapsedTime ms")
        
        removeSession(sessionId)
        
        return testsGrpcClient.completeTestSession(sessionId)
    }

    override fun getTestResults(sessionId: String): Flow<Result<TestResult>> {
        Logger.d("$tag: Getting test results for session ID: $sessionId")
        return testsGrpcClient.getTestResults(sessionId)
    }
    
    override suspend fun saveSessionProgress(sessionId: String, questionIndex: Int, elapsedTimeMillis: Long) {
        Logger.d("$tag: Saving session progress for session ID: $sessionId, question index: $questionIndex, elapsed time: $elapsedTimeMillis ms")
        sessionPrefs.edit()
            .putInt(getProgressKey(sessionId), questionIndex)
            .putLong(getTimestampKey(sessionId), System.currentTimeMillis())
            .putLong(getElapsedTimeKey(sessionId), elapsedTimeMillis)
            .apply()
            
        val sessions = sessionPrefs.getStringSet(INCOMPLETE_SESSIONS_KEY, mutableSetOf<String>()) ?: mutableSetOf()
        sessions.add(sessionId)
        sessionPrefs.edit().putStringSet(INCOMPLETE_SESSIONS_KEY, sessions).apply()
    }
    
    override suspend fun getSessionProgress(sessionId: String): Int? {
        val progress = sessionPrefs.getInt(getProgressKey(sessionId), -1)
        return if (progress >= 0) {
            Logger.d("$tag: Retrieved session progress for session ID: $sessionId, question index: $progress")
            progress
        } else {
            Logger.d("$tag: No saved progress found for session ID: $sessionId")
            null
        }
    }
    
    override suspend fun getSessionElapsedTime(sessionId: String): Long? {
        val elapsedTime = sessionPrefs.getLong(getElapsedTimeKey(sessionId), -1)
        return if (elapsedTime >= 0) {
            Logger.d("$tag: Retrieved elapsed time for session ID: $sessionId: $elapsedTime ms")
            elapsedTime
        } else {
            Logger.d("$tag: No saved elapsed time found for session ID: $sessionId")
            null
        }
    }
    
    override suspend fun getUncompletedSessions(): List<TestSession> {
        val sessionIds = sessionPrefs.getStringSet(INCOMPLETE_SESSIONS_KEY, emptySet()) ?: emptySet()
        Logger.d("$tag: Found ${sessionIds.size} uncompleted test sessions")
        
        val result = mutableListOf<TestSession>()
        for (sessionId in sessionIds) {
            try {
                val session = testsGrpcClient.getTestSession(sessionId).firstOrNull()
                if (session != null && session.isSuccess) {
                    result.add(session.getOrThrow())
                }
            } catch (e: Exception) {
                Logger.e("$tag: Error retrieving uncompleted session $sessionId: ${e.message}")
                removeSession(sessionId)
            }
        }
        return result
    }
    
    override suspend fun removeUncompletedSession(sessionId: String) {
        Logger.d("$tag: Removing uncompleted session: $sessionId")
        removeSession(sessionId)
    }
    
    private fun getProgressKey(sessionId: String) = "progress_$sessionId"
    private fun getTimestampKey(sessionId: String) = "timestamp_$sessionId"
    private fun getElapsedTimeKey(sessionId: String) = "elapsed_time_$sessionId"
    
    private fun removeSession(sessionId: String) {
        val sessions = sessionPrefs.getStringSet(INCOMPLETE_SESSIONS_KEY, mutableSetOf())?.toMutableSet() ?: mutableSetOf()
        sessions.remove(sessionId)
        sessionPrefs.edit()
            .putStringSet(INCOMPLETE_SESSIONS_KEY, sessions)
            .remove(getProgressKey(sessionId))
            .remove(getTimestampKey(sessionId))
            .remove(getElapsedTimeKey(sessionId))
            .apply()
    }
    
    companion object {
        private const val INCOMPLETE_SESSIONS_KEY = "incomplete_sessions"
    }
}
