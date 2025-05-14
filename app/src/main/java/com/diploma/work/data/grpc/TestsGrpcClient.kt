package com.diploma.work.data.grpc

import com.diploma.work.data.AppSession
import com.diploma.work.data.models.*
import com.diploma.work.data.models.Direction
import com.diploma.work.data.models.Level
import com.diploma.work.data.models.Technology
import com.diploma.work.data.models.TestSession
import com.diploma.work.grpc.tests.CompleteTestSessionRequest
import com.diploma.work.grpc.tests.GetTechnologiesRequest
import com.diploma.work.grpc.tests.GetTestRequest
import com.diploma.work.grpc.tests.GetTestResultsRequest
import com.diploma.work.grpc.tests.GetTestSessionRequest
import com.diploma.work.grpc.tests.GetTestsByTechnologyRequest
import com.diploma.work.grpc.tests.GetTestsRequest
import com.diploma.work.grpc.tests.Question
import com.diploma.work.grpc.tests.SaveAnswerRequest
import com.diploma.work.grpc.tests.StartTestSessionRequest
import com.diploma.work.grpc.tests.TestInfo
import com.diploma.work.grpc.tests.TestServiceGrpc
import com.orhanobut.logger.Logger
import io.grpc.ManagedChannel
import io.grpc.StatusRuntimeException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject
import javax.inject.Singleton
import java.util.concurrent.ConcurrentHashMap
import kotlinx.coroutines.runBlocking

@Singleton
class TestsGrpcClient @Inject constructor(
    private val channel: ManagedChannel,
    private val session: AppSession,
    private val context: android.content.Context
) {
    private val stub: TestServiceGrpc.TestServiceBlockingStub = TestServiceGrpc.newBlockingStub(channel)
    private val tag = "grpc.tests"

    private val completeTestMutex = Mutex()

    private val completedSessions = ConcurrentHashMap<String, Boolean>()

    fun getTechnologies(direction: Direction? = null): Flow<Result<List<Technology>>> = flow {
        try {
            Logger.d("$tag: Getting technologies list" + (direction?.let { " for direction: $it" } ?: ""))
            val requestBuilder = GetTechnologiesRequest.newBuilder()

            if (direction != null) {
                requestBuilder.direction = direction.toProtoDirection()
            }

            val request = requestBuilder.build()
            val response = stub.getTechnologies(request)

            val technologies = response.technologiesList.map { protoTech ->
                Technology(
                    id = protoTech.id,
                    name = protoTech.name,
                    description = protoTech.description,
                    direction = protoTech.direction.toModelDirection()
                )
            }
            Logger.d("$tag: Retrieved ${technologies.size} technologies")
            Logger.v("$tag: Technologies: ${technologies.joinToString { it.name }}")
            emit(Result.success(technologies))
        } catch (e: StatusRuntimeException) {
            Logger.e("$tag: Failed to get technologies with gRPC error: ${e.status.code} - ${e.status.description}")
            emit(Result.failure(e))
        } catch (e: Exception) {
            Logger.e("$tag: Failed to get technologies: ${e.message}")
            emit(Result.failure(e))
        }
    }.flowOn(Dispatchers.IO)

    fun getTests(
        direction: Direction? = null,
        level: Level? = null,
        includeUnpublished: Boolean = false
    ): Flow<Result<List<com.diploma.work.data.models.TestInfo>>> = flow {
        try {
            Logger.d("$tag: Getting tests list" +
                (direction?.let { " for direction: $it" } ?: "") +
                (level?.let { ", level: $it" } ?: "") +
                ", includeUnpublished: $includeUnpublished")

            val requestBuilder = GetTestsRequest.newBuilder()
                .setIncludeUnpublished(includeUnpublished)

            if (direction != null) {
                requestBuilder.direction = direction.toProtoDirection()
            }

            if (level != null) {
                requestBuilder.level = level.toProtoLevel()
            }

            val request = requestBuilder.build()
            val response = stub.getTests(request)

            val tests = response.testsList.map { protoTest ->
                mapProtoTestInfoToModel(protoTest)
            }
            Logger.d("$tag: Retrieved ${tests.size} tests")
            Logger.v("$tag: Tests: ${tests.joinToString { it.title }}")
            emit(Result.success(tests))
        } catch (e: StatusRuntimeException) {
            Logger.e("$tag: Failed to get tests with gRPC error: ${e.status.code} - ${e.status.description}")
            emit(Result.failure(e))
        } catch (e: Exception) {
            Logger.e("$tag: Failed to get tests: ${e.message}")
            emit(Result.failure(e))
        }
    }.flowOn(Dispatchers.IO)

    fun getTestsByTechnology(
        technologyId: Long,
        level: Level? = null,
        includeUnpublished: Boolean = false
    ): Flow<Result<List<com.diploma.work.data.models.TestInfo>>> = flow {
        try {
            Logger.d("$tag: Getting tests for technology ID: $technologyId" +
                (level?.let { ", level: $it" } ?: "") +
                ", includeUnpublished: $includeUnpublished")

            val requestBuilder = GetTestsByTechnologyRequest.newBuilder()
                .setTechnologyId(technologyId)
                .setIncludeUnpublished(includeUnpublished)

            if (level != null) {
                requestBuilder.level = level.toProtoLevel()
            }

            val request = requestBuilder.build()
            val response = stub.getTestsByTechnology(request)

            val tests = response.testsList.map { protoTest ->
                mapProtoTestInfoToModel(protoTest)
            }
            Logger.d("$tag: Retrieved ${tests.size} tests for technology ID: $technologyId")
            Logger.v("$tag: Tests: ${tests.joinToString { it.title }}")
            emit(Result.success(tests))
        } catch (e: StatusRuntimeException) {
            Logger.e("$tag: Failed to get tests for technology ID: $technologyId with gRPC error: ${e.status.code} - ${e.status.description}")
            emit(Result.failure(e))
        } catch (e: Exception) {
            Logger.e("$tag: Failed to get tests for technology ID: $technologyId: ${e.message}")
            emit(Result.failure(e))
        }
    }.flowOn(Dispatchers.IO)

    fun getTest(testId: Long): Flow<Result<Test>> = flow {
        try {
            Logger.d("$tag: Getting test details for test ID: $testId")
            val request = GetTestRequest.newBuilder()
                .setTestId(testId)
                .build()

            val response = stub.getTest(request)

            val testInfo = mapProtoTestInfoToModel(response.test)

            val questions = response.questionsList.map { protoQuestion ->
                mapProtoQuestionToModel(protoQuestion)
            }

            val test = Test(
                info = testInfo,
                questions = questions
            )

            Logger.d("$tag: Retrieved test '${testInfo.title}' with ${questions.size} questions")
            emit(Result.success(test))
        } catch (e: StatusRuntimeException) {
            Logger.e("$tag: Failed to get test ID: $testId with gRPC error: ${e.status.code} - ${e.status.description}")
            emit(Result.failure(e))
        } catch (e: Exception) {
            Logger.e("$tag: Failed to get test ID: $testId: ${e.message}")
            emit(Result.failure(e))
        }
    }.flowOn(Dispatchers.IO)

    fun startTestSession(testId: Long): Flow<Result<TestSession>> = flow {
        try {
            val userId = session.getUserId() ?: 0L
            Logger.d("$tag: Starting test session for test ID: $testId, user ID: $userId")

            val testRequest = GetTestRequest.newBuilder()
                .setTestId(testId)
                .build()
            
            val testResponse = stub.getTest(testRequest)
            val testInfo = mapProtoTestInfoToModel(testResponse.test)

            val request = StartTestSessionRequest.newBuilder()
                .setTestId(testId)
                .setUserId(userId)
                .build()

            val response = stub.startTestSession(request)
            val testSession = TestSession(
                sessionId = response.sessionId,
                testId = testId,
                questions = emptyList(),
                startedAt = System.currentTimeMillis(),
                testInfo = testInfo
            )
            Logger.d("$tag: Test session started successfully with session ID: ${response.sessionId}")
            emit(Result.success(testSession))
        } catch (e: StatusRuntimeException) {
            Logger.e("$tag: Failed to start test session for test ID: $testId with gRPC error: ${e.status.code} - ${e.status.description}")
            emit(Result.failure(e))
        } catch (e: Exception) {
            Logger.e("$tag: Failed to start test session for test ID: $testId: ${e.message}")
            emit(Result.failure(e))
        }
    }.flowOn(Dispatchers.IO)

    fun getTestSession(sessionId: String): Flow<Result<TestSession>> = flow {
        try {
            Logger.d("$tag: Getting test session for session ID: $sessionId")
            val request = GetTestSessionRequest.newBuilder()
                .setSessionId(sessionId)
                .build()

            val response = stub.getTestSession(request)

            val testInfo = mapProtoTestInfoToModel(response.test)

            val questionsList = response.questionsList.map { protoQuestion ->
                mapProtoQuestionToModel(protoQuestion)
            }

            val answers = HashMap<Long, Answer>()

            val startTime = System.currentTimeMillis()

            val testSession = TestSession(
                sessionId = sessionId,
                testId = response.test.id,
                questions = questionsList,
                startedAt = startTime,
                answers = answers,
                testInfo = testInfo
            )
            Logger.d("$tag: Retrieved test session for test '${testInfo.title}' with ${questionsList.size} questions")
            emit(Result.success(testSession))
        } catch (e: StatusRuntimeException) {
            Logger.e("$tag: Failed to get test session ID: $sessionId with gRPC error: ${e.status.code} - ${e.status.description}")
            emit(Result.failure(e))
        } catch (e: Exception) {
            Logger.e("$tag: Failed to get test session ID: $sessionId: ${e.message}")
            emit(Result.failure(e))
        }
    }.flowOn(Dispatchers.IO)

    fun saveAnswer(
        sessionId: String,
        answer: Answer
    ): Flow<Result<Boolean>> = flow {
        try {
            Logger.d("$tag: Saving answer for session ID: $sessionId, question ID: ${answer.questionId}")
            
            val answerType = when {
                answer.selectedOptions.isNotEmpty() -> "selection"
                !answer.textAnswer.isNullOrBlank() -> "text"
                !answer.codeAnswer.isNullOrBlank() -> "code"
                else -> "empty"
            }
            Logger.d("$tag: Answer type: $answerType")
            
            val selectedOption = if (answer.selectedOptions.isNotEmpty()) {
                answer.selectedOptions[0]
            } else {
                0
            }

            val requestBuilder = SaveAnswerRequest.newBuilder()
                .setSessionId(sessionId)
                .setQuestionId(answer.questionId)
                .setSelectedOption(selectedOption)

            val request = requestBuilder.build()
            val response = stub.saveAnswer(request)
            Logger.d("$tag: Answer saved successfully for session ID: $sessionId, question ID: ${answer.questionId}")

            emit(Result.success(true))
        } catch (e: StatusRuntimeException) {
            Logger.e("$tag: Failed to save answer for session ID: $sessionId with gRPC error: ${e.status.code} - ${e.status.description}")
            emit(Result.failure(e))
        } catch (e: Exception) {
            Logger.e("$tag: Failed to save answer for session ID: $sessionId: ${e.message}")
            emit(Result.failure(e))
        }
    }.flowOn(Dispatchers.IO)

    private fun createAnswerObject(
        questionId: Long,
        selectedOptions: List<Int>,
        textAnswer: String?,
        codeAnswer: String?
    ): Answer {
        return Answer(
            questionId = questionId,
            selectedOptions = selectedOptions,
            textAnswer = textAnswer,
            codeAnswer = codeAnswer
        )
    }

    fun completeTestSession(
        sessionId: String
    ): Flow<Result<TestResult>> = flow<Result<TestResult>> {
        if (completedSessions.containsKey(sessionId)) {
            Logger.w("$tag: Session $sessionId already completed, getting test results instead")
            try {
                val request = GetTestResultsRequest.newBuilder()
                    .setSubmissionId(sessionId)
                    .build()
                
                val response = stub.getTestResults(request)
                
                val sessionPrefs = context.getSharedPreferences("test_sessions_prefs", android.content.Context.MODE_PRIVATE)
                val elapsedTime = sessionPrefs.getLong("elapsed_time_$sessionId", 0L)
                
                val result = TestResult(
                    score = response.score,
                    totalPoints = response.totalPoints,
                    feedback = response.feedback,
                    questionResults = response.questionResultsList.map { 
                        mapProtoQuestionResultToModel(it)
                    },
                    durationMillis = elapsedTime
                )
                Logger.d("$tag: Retrieved test results for completed session ID: $sessionId with duration: $elapsedTime ms")
                emit(Result.success(result))
            } catch (e: Exception) {
                Logger.e("$tag: Failed to get results for already completed session: $sessionId, error: ${e.message}")
                emit(Result.failure(e))
            }
            return@flow
        }
        
        completeTestMutex.withLock {
            if (completedSessions.containsKey(sessionId)) {
                Logger.w("$tag: Session $sessionId already completed (checked in lock), getting test results instead")
                try {
                    val request = GetTestResultsRequest.newBuilder()
                        .setSubmissionId(sessionId)
                        .build()
                    
                    val response = stub.getTestResults(request)
                    
                    val sessionPrefs = context.getSharedPreferences("test_sessions_prefs", android.content.Context.MODE_PRIVATE)
                    val elapsedTime = sessionPrefs.getLong("elapsed_time_$sessionId", 0L)
                    
                    val result = TestResult(
                        score = response.score,
                        totalPoints = response.totalPoints,
                        feedback = response.feedback,
                        questionResults = response.questionResultsList.map { 
                            mapProtoQuestionResultToModel(it)
                        },
                        durationMillis = elapsedTime
                    )
                    Logger.d("$tag: Retrieved test results for completed session ID: $sessionId with duration: $elapsedTime ms")
                    emit(Result.success(result))
                } catch (e: Exception) {
                    Logger.e("$tag: Failed to get results for already completed session: $sessionId, error: ${e.message}")
                    emit(Result.failure(e))
                }
                return@withLock
            }
            
            try {
                Logger.d("$tag: Completing test session for session ID: $sessionId")
                val request = CompleteTestSessionRequest.newBuilder()
                    .setSessionId(sessionId)
                    .build()

                val response = stub.completeTestSession(request)
                
                val testResultsRequest = GetTestResultsRequest.newBuilder()
                    .setSubmissionId(sessionId)
                    .build()
                    
                val testResults = stub.getTestResults(testResultsRequest)
                
                val result = TestResult(
                    score = testResults.score,
                    totalPoints = testResults.totalPoints,
                    feedback = testResults.feedback,
                    questionResults = testResults.questionResultsList.map { 
                        mapProtoQuestionResultToModel(it)
                    }
                )
                
                completedSessions[sessionId] = true
                
                Logger.d("$tag: Test session completed successfully for session ID: $sessionId")
                Logger.d("$tag: Test result score: ${result.score}/${result.totalPoints}")
                emit(Result.success<TestResult>(result))
            } catch (e: StatusRuntimeException) {
                if (e.status.description?.contains("session already completed") == true) {
                    completedSessions[sessionId] = true
                    
                    try {
                        val testResultsRequest = GetTestResultsRequest.newBuilder()
                            .setSubmissionId(sessionId)
                            .build()
                            
                        val testResults = stub.getTestResults(testResultsRequest)
                        
                        val result = TestResult(
                            score = testResults.score,
                            totalPoints = testResults.totalPoints,
                            feedback = testResults.feedback,
                            questionResults = testResults.questionResultsList.map { 
                                mapProtoQuestionResultToModel(it)
                            }
                        )
                        
                        Logger.d("$tag: Retrieved results for already completed session ID: $sessionId")
                        emit(Result.success(result))
                        return@withLock
                    } catch (innerE: Exception) {
                        Logger.e("$tag: Failed to get results for already completed session: $sessionId, error: ${innerE.message}")
                        emit(Result.failure(innerE))
                        return@withLock
                    }
                }
                
                Logger.e("$tag: Failed to complete test session ID: $sessionId with gRPC error: ${e.status.code} - ${e.status.description}")
                emit(Result.failure(e))
            } catch (e: Exception) {
                Logger.e("$tag: Failed to complete test session ID: $sessionId: ${e.message}")
                emit(Result.failure(e))
            }
        }
    }.flowOn(Dispatchers.IO)
    
    fun getTestResults(
        sessionId: String
    ): Flow<Result<TestResult>> = flow {
        try {
            Logger.d("$tag: Getting test results for session ID: $sessionId")
            val request = GetTestResultsRequest.newBuilder()
                .setSubmissionId(sessionId)
                .build()
            
            val response = stub.getTestResults(request)
            
            val sessionPrefs = context.getSharedPreferences("test_sessions_prefs", android.content.Context.MODE_PRIVATE)
            val elapsedTime = sessionPrefs.getLong("elapsed_time_$sessionId", 0L)
            
            val result = TestResult(
                score = response.score,
                totalPoints = response.totalPoints,
                feedback = response.feedback,
                questionResults = response.questionResultsList.map { 
                    mapProtoQuestionResultToModel(it)
                },
                durationMillis = elapsedTime
            )
            Logger.d("$tag: Retrieved test results successfully for session ID: $sessionId with duration: $elapsedTime ms")
            emit(Result.success(result))
        } catch (e: StatusRuntimeException) {
            Logger.e("$tag: Failed to get test results for session ID: $sessionId with gRPC error: ${e.status.code} - ${e.status.description}")
            emit(Result.failure(e))
        } catch (e: Exception) {
            Logger.e("$tag: Failed to get test results for session ID: $sessionId: ${e.message}")
            emit(Result.failure(e))
        }
    }.flowOn(Dispatchers.IO)
    
    private fun mapProtoQuestionResultToModel(protoResult: com.diploma.work.grpc.tests.QuestionResult): QuestionResult {
        // Логируем ответы для отладки
        Logger.d("$tag: Mapping question result: correct='${protoResult.correctAnswer}', user='${protoResult.userAnswer}'")
        
        return QuestionResult(
            questionId = protoResult.questionId,
            isCorrect = protoResult.isCorrect,
            pointsEarned = protoResult.pointsEarned,
            feedback = protoResult.feedback,
            correctAnswer = protoResult.correctAnswer.trim(),
            userAnswer = protoResult.userAnswer.trim()
        )
    }

    private fun mapProtoTestInfoToModel(protoTest: TestInfo): com.diploma.work.data.models.TestInfo {
        return TestInfo(
            id = protoTest.id,
            title = protoTest.title,
            description = protoTest.description,
            direction = protoTest.direction.toModelDirection(),
            level = protoTest.level.toModelLevel(),
            technologyId = protoTest.technologyId,
            technologyName = protoTest.technologyName,
            isPublished = protoTest.isPublished,
            questionsCount = protoTest.questionsCount
        )
    }

    private fun mapProtoQuestionToModel(protoQuestion: Question): com.diploma.work.data.models.Question {
        return Question(
            id = protoQuestion.id,
            text = protoQuestion.text,
            type = protoQuestion.type.toModelQuestionType(),
            options = protoQuestion.optionsList,
            correctOptions = protoQuestion.correctOptionsList,
            sampleCode = if (protoQuestion.sampleCode.isNotEmpty()) protoQuestion.sampleCode else null,
            points = protoQuestion.points,
            explanation = protoQuestion.explanation
        )
    }

    private fun Direction.toProtoDirection(): com.diploma.work.grpc.tests.Direction {
        return when (this) {
            Direction.UNSPECIFIED -> com.diploma.work.grpc.tests.Direction.DIRECTION_UNSPECIFIED
            Direction.BACKEND -> com.diploma.work.grpc.tests.Direction.BACKEND
            Direction.FRONTEND -> com.diploma.work.grpc.tests.Direction.FRONTEND
            Direction.DEVOPS -> com.diploma.work.grpc.tests.Direction.DEVOPS
            Direction.DATA_SCIENCE -> com.diploma.work.grpc.tests.Direction.DATA_SCIENCE
        }
    }

    private fun Level.toProtoLevel(): com.diploma.work.grpc.tests.Level {
        return when (this) {
            Level.UNSPECIFIED -> com.diploma.work.grpc.tests.Level.LEVEL_UNSPECIFIED
            Level.JUNIOR -> com.diploma.work.grpc.tests.Level.JUNIOR
            Level.MIDDLE -> com.diploma.work.grpc.tests.Level.MIDDLE
            Level.SENIOR -> com.diploma.work.grpc.tests.Level.SENIOR
        }
    }
}

fun com.diploma.work.grpc.tests.Direction.toModelDirection(): Direction {
    return when (this) {
        com.diploma.work.grpc.tests.Direction.BACKEND -> Direction.BACKEND
        com.diploma.work.grpc.tests.Direction.FRONTEND -> Direction.FRONTEND
        com.diploma.work.grpc.tests.Direction.DEVOPS -> Direction.DEVOPS
        com.diploma.work.grpc.tests.Direction.DATA_SCIENCE -> Direction.DATA_SCIENCE
        else -> Direction.UNSPECIFIED
    }
}

fun com.diploma.work.grpc.tests.Level.toModelLevel(): Level {
    return when (this) {
        com.diploma.work.grpc.tests.Level.JUNIOR -> Level.JUNIOR
        com.diploma.work.grpc.tests.Level.MIDDLE -> Level.MIDDLE
        com.diploma.work.grpc.tests.Level.SENIOR -> Level.SENIOR
        else -> Level.UNSPECIFIED
    }
}

fun com.diploma.work.grpc.tests.QuestionType.toModelQuestionType(): QuestionType {
    return when (this) {
        com.diploma.work.grpc.tests.QuestionType.MULTIPLE_CHOICE -> QuestionType.MULTIPLE_CHOICE
        com.diploma.work.grpc.tests.QuestionType.SINGLE_CHOICE -> QuestionType.SINGLE_CHOICE
        com.diploma.work.grpc.tests.QuestionType.TEXT -> QuestionType.TEXT
        com.diploma.work.grpc.tests.QuestionType.CODE -> QuestionType.CODE
        else -> QuestionType.UNSPECIFIED
    }
}
