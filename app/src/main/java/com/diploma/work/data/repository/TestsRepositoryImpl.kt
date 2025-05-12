package com.diploma.work.data.repository

import com.diploma.work.data.grpc.TestsGrpcClient
import com.diploma.work.data.models.*
import kotlinx.coroutines.flow.Flow
import com.diploma.work.data.models.Direction
import com.diploma.work.data.models.Level
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TestsRepositoryImpl @Inject constructor(
    private val testsGrpcClient: TestsGrpcClient
) : TestsRepository {

    override fun getTechnologies(direction: Direction?): Flow<Result<List<Technology>>> {
        return testsGrpcClient.getTechnologies(direction)
    }

    override fun getTests(
        direction: Direction?,
        level: Level?,
        includeUnpublished: Boolean
    ): Flow<Result<List<TestInfo>>> {
        return testsGrpcClient.getTests(direction, level, includeUnpublished)
    }

    override fun getTestsByTechnology(
        technologyId: Long,
        level: Level?,
        includeUnpublished: Boolean
    ): Flow<Result<List<TestInfo>>> {
        return testsGrpcClient.getTestsByTechnology(technologyId, level, includeUnpublished)
    }

    override fun getTest(testId: Long): Flow<Result<Test>> {
        return testsGrpcClient.getTest(testId)
    }

    override fun startTestSession(testId: Long): Flow<Result<TestSession>> {
        return testsGrpcClient.startTestSession(testId)
    }

    override fun getTestSession(sessionId: String): Flow<Result<TestSession>> {
        return testsGrpcClient.getTestSession(sessionId)
    }

    override fun saveAnswer(sessionId: String, answer: Answer): Flow<Result<Boolean>> {
        return testsGrpcClient.saveAnswer(sessionId, answer)
    }

    override fun completeTestSession(sessionId: String): Flow<Result<TestResult>> {
        return testsGrpcClient.completeTestSession(sessionId)
    }
}
