package com.diploma.work.data.api.dto

import com.diploma.work.data.models.Direction
import com.diploma.work.data.models.Level
import com.diploma.work.data.models.QuestionType
import com.diploma.work.data.models.Question
import com.diploma.work.data.models.QuestionResult
import com.diploma.work.data.models.Technology
import com.diploma.work.data.models.Test
import com.diploma.work.data.models.TestInfo
import com.diploma.work.data.models.TestResult
import com.diploma.work.data.models.TestSession

data class GetTechnologiesDto(
    val technologies: List<TechnologyDto> = emptyList(),
    val nextPageToken: String = ""
)

data class TechnologyDto(
    val id: Long = 0,
    val name: String = "",
    val description: String = "",
    val direction: String = ""
) {
    fun toModel(): Technology = Technology(
        id = id,
        name = name,
        description = description,
        direction = parseDirection(direction)
    )
}

data class GetTestsDto(
    val tests: List<TestInfoDto> = emptyList(),
    val nextPageToken: String = ""
)

data class TestInfoDto(
    val id: Long = 0,
    val title: String = "",
    val description: String = "",
    val direction: String = "",
    val level: String = "",
    val technologyId: Long = 0,
    val technologyName: String = "",
    val isPublished: Boolean = false,
    val questionsCount: Int = 0
) {
    fun toModel(): TestInfo = TestInfo(
        id = id,
        title = title,
        description = description,
        direction = parseDirection(direction),
        level = parseLevel(level),
        technologyId = technologyId,
        technologyName = technologyName,
        isPublished = isPublished,
        questionsCount = questionsCount
    )
}

data class QuestionDto(
    val id: Long = 0,
    val text: String = "",
    val type: String = "",
    val options: List<String> = emptyList(),
    val correctOptions: List<Int> = emptyList(),
    val sampleCode: String = "",
    val points: Int = 0,
    val explanation: String = ""
) {
    fun toModel(): Question = Question(
        id = id,
        text = text,
        type = parseQuestionType(type),
        options = options,
        correctOptions = correctOptions,
        sampleCode = sampleCode.ifEmpty { null },
        points = points,
        explanation = explanation
    )
}

data class GetTestDto(
    val test: TestInfoDto = TestInfoDto(),
    val questions: List<QuestionDto> = emptyList()
) {
    fun toModel(): Test = Test(
        info = test.toModel(),
        questions = questions.map { it.toModel() }
    )
}

data class StartTestSessionDto(
    val sessionId: String = ""
)

data class GetTestSessionDto(
    val sessionId: String = "",
    val test: TestInfoDto = TestInfoDto(),
    val questions: List<QuestionDto> = emptyList()
) {
    fun toModel(requestSessionId: String = sessionId): TestSession = TestSession(
        sessionId = requestSessionId.ifEmpty { sessionId },
        testId = test.id,
        questions = questions.map { it.toModel() },
        startedAt = System.currentTimeMillis(),
        testInfo = test.toModel()
    )
}

data class SaveAnswerDto(
    val success: Boolean = false
)

data class CompleteTestSessionDto(
    val sessionId: String = "",
    val status: String = ""
)

data class QuestionResultDto(
    val questionId: Long = 0,
    val isCorrect: Boolean = false,
    val pointsEarned: Int = 0,
    val feedback: String = "",
    val correctAnswer: String = "",
    val userAnswer: String = ""
) {
    fun toModel(): QuestionResult = QuestionResult(
        questionId = questionId,
        isCorrect = isCorrect,
        pointsEarned = pointsEarned,
        feedback = feedback,
        correctAnswer = correctAnswer.trim(),
        userAnswer = userAnswer.trim()
    )
}

data class GetTestResultsDto(
    val score: Int = 0,
    val totalPoints: Int = 0,
    val feedback: String = "",
    val questionResults: List<QuestionResultDto> = emptyList()
) {
    fun toModel(durationMillis: Long = 0): TestResult = TestResult(
        score = score,
        totalPoints = totalPoints,
        feedback = feedback,
        questionResults = questionResults.map { it.toModel() },
        durationMillis = durationMillis
    )
}

private fun parseDirection(value: String): Direction = when (value) {
    "BACKEND" -> Direction.BACKEND
    "FRONTEND" -> Direction.FRONTEND
    "DEVOPS" -> Direction.DEVOPS
    "DATA_SCIENCE" -> Direction.DATA_SCIENCE
    else -> Direction.UNSPECIFIED
}

private fun parseLevel(value: String): Level = when (value) {
    "JUNIOR" -> Level.JUNIOR
    "MIDDLE" -> Level.MIDDLE
    "SENIOR" -> Level.SENIOR
    else -> Level.UNSPECIFIED
}

private fun parseQuestionType(value: String): QuestionType = when (value) {
    "MULTIPLE_CHOICE" -> QuestionType.MULTIPLE_CHOICE
    "SINGLE_CHOICE" -> QuestionType.SINGLE_CHOICE
    "TEXT" -> QuestionType.TEXT
    "CODE" -> QuestionType.CODE
    else -> QuestionType.SINGLE_CHOICE
}
