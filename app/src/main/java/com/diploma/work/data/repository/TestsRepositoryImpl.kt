package com.diploma.work.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.diploma.work.data.api.SaveAnswerBody
import com.diploma.work.data.api.StartTestSessionBody
import com.diploma.work.data.api.TestsApi
import com.diploma.work.data.models.Answer
import com.diploma.work.data.models.Direction
import com.diploma.work.data.models.Level
import com.diploma.work.data.models.Technology
import com.diploma.work.data.models.Test
import com.diploma.work.data.models.TestInfo
import com.diploma.work.data.models.TestResult
import com.diploma.work.data.models.TestSession
import com.diploma.work.utils.Constants
import com.diploma.work.utils.ErrorContext
import com.diploma.work.utils.ErrorMessageUtils
import com.orhanobut.logger.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import retrofit2.HttpException
import java.io.IOException
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TestsRepositoryImpl @Inject constructor(
    private val api: TestsApi,
    private val context: Context
) : TestsRepository {

    private val sessionPrefs: SharedPreferences by lazy {
        context.getSharedPreferences(Constants.PrefsKeys.TEST_SESSIONS_PREFS, Context.MODE_PRIVATE)
    }

    private val completeTestMutex = Mutex()
    private val completedSessions = ConcurrentHashMap<String, Boolean>()

    override fun getTechnologies(direction: Direction?): Flow<Result<List<Technology>>> = flow {
        try {
            val directionValue = direction?.ordinal ?: 0
            val response = api.getTechnologies(direction = directionValue)
            emit(Result.success(response.technologies.map { it.toModel() }))
        } catch (e: Exception) {
            emit(Result.failure(wrapError(e, ErrorContext.DATA_LOADING)))
        }
    }.flowOn(Dispatchers.IO)

    override fun getTests(
        direction: Direction?,
        level: Level?,
        includeUnpublished: Boolean
    ): Flow<Result<List<TestInfo>>> = flow {
        try {
            val response = api.getTests(
                direction = direction?.ordinal ?: 0,
                level = level?.ordinal ?: 0,
                includeUnpublished = includeUnpublished
            )
            emit(Result.success(response.tests.map { it.toModel() }))
        } catch (e: Exception) {
            emit(Result.failure(wrapError(e, ErrorContext.DATA_LOADING)))
        }
    }.flowOn(Dispatchers.IO)

    override fun getTestsByTechnology(
        technologyId: Long,
        level: Level?,
        includeUnpublished: Boolean
    ): Flow<Result<List<TestInfo>>> = flow {
        try {
            val response = api.getTestsByTechnology(
                techId = technologyId,
                level = level?.ordinal ?: 0,
                includeUnpublished = includeUnpublished
            )
            emit(Result.success(response.tests.map { it.toModel() }))
        } catch (e: Exception) {
            emit(Result.failure(wrapError(e, ErrorContext.DATA_LOADING)))
        }
    }.flowOn(Dispatchers.IO)

    override fun getTest(testId: Long): Flow<Result<Test>> = flow {
        try {
            val response = api.getTest(testId)
            emit(Result.success(response.toModel()))
        } catch (e: Exception) {
            emit(Result.failure(wrapError(e, ErrorContext.DATA_LOADING)))
        }
    }.flowOn(Dispatchers.IO)

    override fun startTestSession(testId: Long): Flow<Result<TestSession>> = flow {
        try {
            val testResponse = api.getTest(testId)
            val testInfo = testResponse.test.toModel()

            val sessionResponse = api.startTestSession(StartTestSessionBody(testId = testId))
            val testSession = TestSession(
                sessionId = sessionResponse.sessionId,
                testId = testId,
                questions = emptyList(),
                startedAt = System.currentTimeMillis(),
                testInfo = testInfo
            )
            emit(Result.success(testSession))
        } catch (e: Exception) {
            emit(Result.failure(wrapError(e, ErrorContext.TEST_SESSION)))
        }
    }.flowOn(Dispatchers.IO)

    override fun getTestSession(sessionId: String): Flow<Result<TestSession>> = flow {
        try {
            val response = api.getTestSession(sessionId)
            emit(Result.success(response.toModel(sessionId)))
        } catch (e: Exception) {
            emit(Result.failure(wrapError(e, ErrorContext.TEST_SESSION)))
        }
    }.flowOn(Dispatchers.IO)

    override fun saveAnswer(sessionId: String, answer: Answer): Flow<Result<Boolean>> = flow {
        try {
            val selectedOption = if (answer.selectedOptions.isNotEmpty()) {
                answer.selectedOptions[0]
            } else {
                0
            }

            api.saveAnswer(
                sessionId = sessionId,
                request = SaveAnswerBody(
                    questionId = answer.questionId,
                    selectedOption = selectedOption
                )
            )
            emit(Result.success(true))
        } catch (e: Exception) {
            emit(Result.failure(wrapError(e, ErrorContext.TEST_SESSION)))
        }
    }.flowOn(Dispatchers.IO)

    override fun completeTestSession(
        sessionId: String,
        elapsedTimeMillis: Long
    ): Flow<Result<TestResult>> = flow<Result<TestResult>> {
        if (completedSessions.containsKey(sessionId)) {
            try {
                val results = api.getTestResults(sessionId)
                emit(Result.success(results.toModel(elapsedTimeMillis)))
            } catch (e: Exception) {
                emit(Result.failure(e))
            }
            return@flow
        }

        completeTestMutex.withLock {
            if (completedSessions.containsKey(sessionId)) {
                try {
                    val results = api.getTestResults(sessionId)
                    emit(Result.success(results.toModel(elapsedTimeMillis)))
                } catch (e: Exception) {
                    emit(Result.failure(e))
                }
                return@withLock
            }

            try {
                api.completeTestSession(sessionId)
                val results = api.getTestResults(sessionId)
                completedSessions[sessionId] = true
                removeSession(sessionId)
                emit(Result.success(results.toModel(elapsedTimeMillis)))
            } catch (e: HttpException) {
                val errorBody = e.response()?.errorBody()?.string() ?: ""
                if (errorBody.contains("session already completed")) {
                    completedSessions[sessionId] = true
                    try {
                        val results = api.getTestResults(sessionId)
                        emit(Result.success(results.toModel(elapsedTimeMillis)))
                        return@withLock
                    } catch (innerE: Exception) {
                        emit(Result.failure(innerE))
                        return@withLock
                    }
                }
                emit(Result.failure(wrapError(e, ErrorContext.TEST_SESSION)))
            } catch (e: Exception) {
                emit(Result.failure(wrapError(e, ErrorContext.TEST_SESSION)))
            }
        }
    }.flowOn(Dispatchers.IO)

    override fun getTestResults(sessionId: String): Flow<Result<TestResult>> = flow {
        try {
            val results = api.getTestResults(sessionId)
            emit(Result.success(results.toModel()))
        } catch (e: Exception) {
            emit(Result.failure(wrapError(e, ErrorContext.TEST_SESSION)))
        }
    }.flowOn(Dispatchers.IO)

    override suspend fun saveSessionProgress(sessionId: String, questionIndex: Int, elapsedTimeMillis: Long) {
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
        return if (progress >= 0) progress else null
    }

    override suspend fun getSessionElapsedTime(sessionId: String): Long? {
        val elapsedTime = sessionPrefs.getLong(getElapsedTimeKey(sessionId), -1)
        return if (elapsedTime >= 0) elapsedTime else null
    }

    override suspend fun getUncompletedSessions(): List<TestSession> {
        val sessionIds = sessionPrefs.getStringSet(INCOMPLETE_SESSIONS_KEY, emptySet()) ?: emptySet()
        val result = mutableListOf<TestSession>()
        for (sessionId in sessionIds) {
            try {
                val response = api.getTestSession(sessionId)
                result.add(response.toModel(sessionId))
            } catch (e: Exception) {
                Logger.e("Error retrieving uncompleted session $sessionId: ${e.message}")
                removeSession(sessionId)
            }
        }
        return result
    }

    override suspend fun removeUncompletedSession(sessionId: String) {
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
            .apply()
    }

    private fun wrapError(e: Exception, context: ErrorContext): Exception {
        return when (e) {
            is HttpException -> {
                val apiException = com.diploma.work.data.api.ApiErrorParser.parse(e)
                Exception(ErrorMessageUtils.getContextualErrorMessage(apiException, context))
            }
            is IOException -> Exception(ErrorMessageUtils.getContextualErrorMessage(e, ErrorContext.NETWORK))
            else -> Exception(ErrorMessageUtils.getContextualErrorMessage(e, context))
        }
    }

    companion object {
        private const val INCOMPLETE_SESSIONS_KEY = "incomplete_sessions"
    }
}
