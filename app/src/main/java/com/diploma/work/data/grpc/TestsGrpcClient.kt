package com.diploma.work.data.grpc

import com.diploma.work.data.AppSession
import com.diploma.work.data.models.*
import com.diploma.work.data.models.Direction
import com.diploma.work.data.models.Level
import com.diploma.work.data.models.QuestionResult
import com.diploma.work.data.models.Technology
import com.diploma.work.data.models.TestSession
import com.diploma.work.grpc.tests.*
import com.diploma.work.grpc.tests.Answer
import com.diploma.work.grpc.tests.Question
import com.diploma.work.grpc.tests.TestInfo
import io.grpc.ManagedChannel
import io.grpc.StatusRuntimeException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TestsGrpcClient @Inject constructor(
    private val channel: ManagedChannel,
    private val session: AppSession
) {
    private val stub: TestServiceGrpc.TestServiceBlockingStub = TestServiceGrpc.newBlockingStub(channel)

    fun getTechnologies(direction: Direction? = null): Flow<Result<List<Technology>>> = flow {
        try {
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
            emit(Result.success(technologies))
        } catch (e: StatusRuntimeException) {
            emit(Result.failure(e))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }.flowOn(Dispatchers.IO)

    fun getTests(
        direction: Direction? = null,
        level: Level? = null,
        includeUnpublished: Boolean = false
    ): Flow<Result<List<com.diploma.work.data.models.TestInfo>>> = flow {
        try {
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
            emit(Result.success(tests))
        } catch (e: StatusRuntimeException) {
            emit(Result.failure(e))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }.flowOn(Dispatchers.IO)

    fun getTestsByTechnology(
        technologyId: Long,
        level: Level? = null,
        includeUnpublished: Boolean = false
    ): Flow<Result<List<com.diploma.work.data.models.TestInfo>>> = flow {
        try {
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
            emit(Result.success(tests))
        } catch (e: StatusRuntimeException) {
            emit(Result.failure(e))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }.flowOn(Dispatchers.IO)

    fun getTest(testId: Long): Flow<Result<com.diploma.work.data.models.Test>> = flow {
        try {
            val request = GetTestRequest.newBuilder()
                .setTestId(testId)
                .build()

            val response = stub.getTest(request)
            
            val testInfo = mapProtoTestInfoToModel(response.test)
            
            val questions = response.questionsList.map { protoQuestion ->
                mapProtoQuestionToModel(protoQuestion)
            }
            
            val test = com.diploma.work.data.models.Test(
                info = testInfo,
                questions = questions
            )
            
            emit(Result.success(test))
        } catch (e: StatusRuntimeException) {
            emit(Result.failure(e))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }.flowOn(Dispatchers.IO)

    fun startTestSession(testId: Long): Flow<Result<TestSession>> = flow {
        try {
            val userId = session.getUserId() ?: 0L
            
            val request = StartTestSessionRequest.newBuilder()
                .setTestId(testId)
                .setUserId(userId)
                .build()

            val response = stub.startTestSession(request)
            val testSession = TestSession(
                sessionId = response.sessionId,
                testId = testId,
                questions = emptyList(),
                startedAt = System.currentTimeMillis()
            )
            emit(Result.success(testSession))
        } catch (e: StatusRuntimeException) {
            emit(Result.failure(e))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }.flowOn(Dispatchers.IO)

    fun getTestSession(sessionId: String): Flow<Result<TestSession>> = flow {
        try {
            val request = GetTestSessionRequest.newBuilder()
                .setSessionId(sessionId)
                .build()

            val response = stub.getTestSession(request)

            val testInfo = mapProtoTestInfoToModel(response.test)

            val questionsList = response.questionsList.map { protoQuestion ->
                mapProtoQuestionToModel(protoQuestion)
            }

            val answers = HashMap<Long, com.diploma.work.data.models.Answer>()

            val startTime = System.currentTimeMillis()
            
            val testSession = TestSession(
                sessionId = sessionId,
                testId = response.test.id,
                questions = questionsList,
                startedAt = startTime,
                answers = answers
            )
            emit(Result.success(testSession))
        } catch (e: StatusRuntimeException) {
            emit(Result.failure(e))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }.flowOn(Dispatchers.IO)

    fun saveAnswer(
        sessionId: String,
        answer: com.diploma.work.data.models.Answer
    ): Flow<Result<Boolean>> = flow {
        try {
            val selectedOption = if (answer.selectedOptions.isNotEmpty()) {
                answer.selectedOptions[0]
            } else {
                0
            }
            
            val request = SaveAnswerRequest.newBuilder()
                .setSessionId(sessionId)
                .setQuestionId(answer.questionId)
                .setSelectedOption(selectedOption)
                .build()

            val response = stub.saveAnswer(request)

            emit(Result.success(true))
        } catch (e: StatusRuntimeException) {
            emit(Result.failure(e))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }.flowOn(Dispatchers.IO)

    private fun createAnswerObject(
        questionId: Long, 
        selectedOptions: List<Int>,
        textAnswer: String?,
        codeAnswer: String?
    ): com.diploma.work.data.models.Answer {
        return com.diploma.work.data.models.Answer(
            questionId = questionId,
            selectedOptions = selectedOptions,
            textAnswer = textAnswer,
            codeAnswer = codeAnswer
        )
    }

    fun completeTestSession(
        sessionId: String
    ): Flow<Result<TestResult>> = flow {
        try {
            val request = CompleteTestSessionRequest.newBuilder()
                .setSessionId(sessionId)
                .build()

            val response = stub.completeTestSession(request)
            val result = TestResult(
                score = 0,
                totalPoints = 0,
                feedback = response.message,
                questionResults = emptyList()
            )
            emit(Result.success(result))
        } catch (e: StatusRuntimeException) {
            emit(Result.failure(e))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }.flowOn(Dispatchers.IO)
    

    private fun mapProtoTestInfoToModel(protoTest: TestInfo): com.diploma.work.data.models.TestInfo {
        return com.diploma.work.data.models.TestInfo(
            id = protoTest.id,
            title = protoTest.title,
            description = protoTest.description,
            direction = protoTest.direction.toModelDirection(),
            level = protoTest.level.toModelLevel(),
            technologyId = protoTest.technologyId,
            technologyName = protoTest.technologyName,
            isPublished = protoTest.isPublished
        )
    }
    

    private fun mapProtoQuestionToModel(protoQuestion: Question): com.diploma.work.data.models.Question {
        return com.diploma.work.data.models.Question(
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

fun com.diploma.work.grpc.tests.QuestionType.toModelQuestionType(): com.diploma.work.data.models.QuestionType {
    return when (this) {
        com.diploma.work.grpc.tests.QuestionType.MCQ -> com.diploma.work.data.models.QuestionType.MCQ
        com.diploma.work.grpc.tests.QuestionType.TEXT -> com.diploma.work.data.models.QuestionType.TEXT
        com.diploma.work.grpc.tests.QuestionType.CODE -> com.diploma.work.data.models.QuestionType.CODE
        else -> com.diploma.work.data.models.QuestionType.UNSPECIFIED
    }
}
