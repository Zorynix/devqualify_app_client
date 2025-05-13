package com.diploma.work.data.models

enum class Direction {
    UNSPECIFIED,
    BACKEND,
    FRONTEND,
    DEVOPS,
    DATA_SCIENCE
}

enum class Level {
    UNSPECIFIED,
    JUNIOR,
    MIDDLE,
    SENIOR
}

enum class QuestionType {
    UNSPECIFIED,
    MULTIPLE_CHOICE,
    SINGLE_CHOICE,
    TEXT,
    CODE
}

data class Technology(
    val id: Long,
    val name: String,
    val description: String,
    val direction: Direction
)

data class TestInfo(
    val id: Long,
    val title: String,
    val description: String,
    val direction: Direction,
    val level: Level,
    val technologyId: Long,
    val technologyName: String,
    val isPublished: Boolean
)

data class Question(
    val id: Long,
    val text: String,
    val type: QuestionType,
    val options: List<String>,
    val correctOptions: List<Int>,
    val sampleCode: String?,
    val points: Int,
    val explanation: String
)

data class Test(
    val info: TestInfo,
    val questions: List<Question>
)

data class Answer(
    val questionId: Long,
    val selectedOptions: List<Int> = emptyList(),
    val textAnswer: String? = null,
    val codeAnswer: String? = null
)

data class QuestionResult(
    val questionId: Long,
    val isCorrect: Boolean,
    val pointsEarned: Int,
    val feedback: String,
    val correctAnswer: String,
    val userAnswer: String
)

data class TestSession(
    val sessionId: String,
    val testId: Long,
    val questions: List<Question>,
    val startedAt: Long,
    val answers: Map<Long, Answer> = emptyMap()
)

data class TestResult(
    val score: Int,
    val totalPoints: Int,
    val feedback: String,
    val questionResults: List<QuestionResult>
) 