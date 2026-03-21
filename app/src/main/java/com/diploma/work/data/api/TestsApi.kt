package com.diploma.work.data.api

import com.diploma.work.data.api.dto.CompleteTestSessionDto
import com.diploma.work.data.api.dto.GetTestDto
import com.diploma.work.data.api.dto.GetTestResultsDto
import com.diploma.work.data.api.dto.GetTestSessionDto
import com.diploma.work.data.api.dto.GetTestsDto
import com.diploma.work.data.api.dto.GetTechnologiesDto
import com.diploma.work.data.api.dto.SaveAnswerDto
import com.diploma.work.data.api.dto.StartTestSessionDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface TestsApi {

    @GET("tests")
    suspend fun getTests(
        @Query("direction") direction: Int = 0,
        @Query("level") level: Int = 0,
        @Query("include_unpublished") includeUnpublished: Boolean = false,
        @Query("page_size") pageSize: Int = 20,
        @Query("page_token") pageToken: String = ""
    ): GetTestsDto

    @GET("tests/{test_id}")
    suspend fun getTest(@Path("test_id") testId: Long): GetTestDto

    @GET("technologies/tests")
    suspend fun getTechnologies(
        @Query("direction") direction: Int = 0,
        @Query("page_size") pageSize: Int = 50,
        @Query("page_token") pageToken: String = ""
    ): GetTechnologiesDto

    @GET("technologies/tests/{tech_id}/tests")
    suspend fun getTestsByTechnology(
        @Path("tech_id") techId: Long,
        @Query("level") level: Int = 0,
        @Query("include_unpublished") includeUnpublished: Boolean = false,
        @Query("page_size") pageSize: Int = 20,
        @Query("page_token") pageToken: String = ""
    ): GetTestsDto

    @POST("test-sessions")
    suspend fun startTestSession(@Body request: StartTestSessionBody): StartTestSessionDto

    @GET("test-sessions/{session_id}")
    suspend fun getTestSession(@Path("session_id") sessionId: String): GetTestSessionDto

    @POST("test-sessions/{session_id}/answers")
    suspend fun saveAnswer(
        @Path("session_id") sessionId: String,
        @Body request: SaveAnswerBody
    ): SaveAnswerDto

    @POST("test-sessions/{session_id}/complete")
    suspend fun completeTestSession(@Path("session_id") sessionId: String): CompleteTestSessionDto

    @GET("tests/results/{submission_id}")
    suspend fun getTestResults(@Path("submission_id") submissionId: String): GetTestResultsDto
}

data class StartTestSessionBody(
    val testId: Long
)

data class SaveAnswerBody(
    val questionId: Long,
    val selectedOption: Int
)
