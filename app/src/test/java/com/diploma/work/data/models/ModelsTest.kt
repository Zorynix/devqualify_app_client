package com.diploma.work.data.models

import org.junit.Test
import org.junit.Assert.*
import java.time.Instant

class ModelsTest {
    
    @Test
    fun `TestInfo creation with valid data`() {
        val testInfo = TestInfo(
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
        
        assertEquals(1L, testInfo.id)
        assertEquals("Kotlin Basics", testInfo.title)
        assertEquals("Basic Kotlin test", testInfo.description)
        assertEquals(Direction.BACKEND, testInfo.direction)
        assertEquals(Level.JUNIOR, testInfo.level)
        assertEquals(1L, testInfo.technologyId)
        assertEquals("Kotlin", testInfo.technologyName)
        assertTrue(testInfo.isPublished)
        assertEquals(10, testInfo.questionsCount)
    }
    
    @Test
    fun `Question creation with multiple choice type`() {
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
        
        assertEquals(1L, question.id)
        assertEquals("What is Kotlin?", question.text)
        assertEquals(QuestionType.MULTIPLE_CHOICE, question.type)
        assertEquals(3, question.options.size)
        assertEquals(listOf(0), question.correctOptions)
        assertNull(question.sampleCode)
        assertEquals(1, question.points)
        assertEquals("Kotlin is a programming language", question.explanation)
    }
    
    @Test
    fun `Answer creation with selected options`() {
        val answer = Answer(
            questionId = 1L,
            selectedOptions = listOf(0, 2),
            textAnswer = null,
            codeAnswer = null
        )
        
        assertEquals(1L, answer.questionId)
        assertEquals(listOf(0, 2), answer.selectedOptions)
        assertNull(answer.textAnswer)
        assertNull(answer.codeAnswer)
    }
    
    @Test
    fun `Answer creation with text answer`() {
        val answer = Answer(
            questionId = 2L,
            selectedOptions = emptyList(),
            textAnswer = "Kotlin is a modern programming language",
            codeAnswer = null
        )
        
        assertEquals(2L, answer.questionId)
        assertTrue(answer.selectedOptions.isEmpty())
        assertEquals("Kotlin is a modern programming language", answer.textAnswer)
        assertNull(answer.codeAnswer)
    }
    
    @Test
    fun `TestSession creation with questions`() {
        val questions = listOf(
            Question(
                id = 1L,
                text = "Question 1",
                type = QuestionType.MULTIPLE_CHOICE,
                options = listOf("A", "B"),
                correctOptions = listOf(0),
                sampleCode = null,
                points = 1,
                explanation = "Explanation 1"
            )
        )
        
        val testSession = TestSession(
            sessionId = "session123",
            testId = 1L,
            questions = questions,
            startedAt = System.currentTimeMillis(),
            answers = mapOf(1L to Answer(1L, listOf(0), null, null))
        )
        
        assertEquals("session123", testSession.sessionId)
        assertEquals(1L, testSession.testId)
        assertEquals(1, testSession.questions.size)
        assertEquals(1, testSession.answers.size)
        assertTrue(testSession.startedAt > 0)
    }
    
    @Test
    fun `TestResult creation with question results`() {
        val questionResults = listOf(
            QuestionResult(
                questionId = 1L,
                isCorrect = true,
                pointsEarned = 1,
                feedback = "Correct!",
                correctAnswer = "Language",
                userAnswer = "Language"
            ),
            QuestionResult(
                questionId = 2L,
                isCorrect = false,
                pointsEarned = 0,
                feedback = "Incorrect",
                correctAnswer = "Framework",
                userAnswer = "Library"
            )
        )
        
        val testResult = TestResult(
            score = 50,
            totalPoints = 100,
            feedback = "Good effort!",
            questionResults = questionResults,
            durationMillis = 120000L
        )
        
        assertEquals(50, testResult.score)
        assertEquals(100, testResult.totalPoints)
        assertEquals("Good effort!", testResult.feedback)
        assertEquals(2, testResult.questionResults.size)
        assertEquals(120000L, testResult.durationMillis)
    }
    
    @Test
    fun `Technology creation with direction`() {
        val technology = Technology(
            id = 1L,
            name = "Kotlin",
            description = "Modern programming language",
            direction = Direction.BACKEND
        )
        
        assertEquals(1L, technology.id)
        assertEquals("Kotlin", technology.name)
        assertEquals("Modern programming language", technology.description)
        assertEquals(Direction.BACKEND, technology.direction)
    }
    
    @Test
    fun `Article creation with all fields`() {
        val publishedAt = Instant.now()
        val createdAt = Instant.now()
        
        val article = Article(
            id = 1L,
            title = "Kotlin Best Practices",
            description = "Learn Kotlin best practices",
            content = "Detailed content about Kotlin",
            url = "https://example.com/kotlin-practices",
            author = "John Doe",
            publishedAt = publishedAt,
            createdAt = createdAt,
            rssSourceId = 1L,
            rssSourceName = "Tech Blog",
            technologyIds = listOf(1L, 2L),
            tags = listOf("kotlin", "programming", "best-practices"),
            status = ArticleStatus.PUBLISHED,
            imageUrl = "https://example.com/image.jpg",
            readTimeMinutes = 5
        )
        
        assertEquals(1L, article.id)
        assertEquals("Kotlin Best Practices", article.title)
        assertEquals("Learn Kotlin best practices", article.description)
        assertEquals("Detailed content about Kotlin", article.content)
        assertEquals("https://example.com/kotlin-practices", article.url)
        assertEquals("John Doe", article.author)
        assertEquals(publishedAt, article.publishedAt)
        assertEquals(createdAt, article.createdAt)
        assertEquals(1L, article.rssSourceId)
        assertEquals("Tech Blog", article.rssSourceName)
        assertEquals(listOf(1L, 2L), article.technologyIds)
        assertEquals(listOf("kotlin", "programming", "best-practices"), article.tags)
        assertEquals(ArticleStatus.PUBLISHED, article.status)
        assertEquals("https://example.com/image.jpg", article.imageUrl)
        assertEquals(5, article.readTimeMinutes)
    }
    
    @Test
    fun `UserPreferences creation with all settings`() {
        val updatedAt = Instant.now()
        
        val preferences = UserPreferences(
            userId = 123L,
            technologyIds = listOf(1L, 2L, 3L),
            directions = listOf(ArticleDirection.BACKEND, ArticleDirection.FRONTEND),
            deliveryFrequency = DeliveryFrequency.WEEKLY,
            emailNotifications = true,
            pushNotifications = false,
            excludedSources = listOf("source1", "source2"),
            articlesPerDay = 5,
            updatedAt = updatedAt
        )
        
        assertEquals(123L, preferences.userId)
        assertEquals(listOf(1L, 2L, 3L), preferences.technologyIds)
        assertEquals(listOf(ArticleDirection.BACKEND, ArticleDirection.FRONTEND), preferences.directions)
        assertEquals(DeliveryFrequency.WEEKLY, preferences.deliveryFrequency)
        assertTrue(preferences.emailNotifications)
        assertFalse(preferences.pushNotifications)
        assertEquals(listOf("source1", "source2"), preferences.excludedSources)
        assertEquals(5, preferences.articlesPerDay)
        assertEquals(updatedAt, preferences.updatedAt)
    }
    
    @Test
    fun `User creation with achievements`() {
        val achievements = listOf(
            Achievement(
                id = 1L,
                name = "First Test",
                description = "Completed first test",
                iconUrl = "https://example.com/icon1.png",
                dateEarned = "2023-01-01"
            )
        )
        
        val user = User(
            id = 1L,
            username = "testuser",
            email = "test@example.com",
            direction = com.diploma.work.grpc.userinfo.Direction.BACKEND,
            level = com.diploma.work.grpc.userinfo.Level.JUNIOR,
            totalCorrectAnswers = 80,
            totalIncorrectAnswers = 20,
            completedTestsCount = 5,
            achievementsCount = 1,
            achievements = achievements,
            avatarUrl = "https://example.com/avatar.jpg"
        )
        
        assertEquals(1L, user.id)
        assertEquals("testuser", user.username)
        assertEquals("test@example.com", user.email)
        assertEquals(com.diploma.work.grpc.userinfo.Direction.BACKEND, user.direction)
        assertEquals(com.diploma.work.grpc.userinfo.Level.JUNIOR, user.level)
        assertEquals(80, user.totalCorrectAnswers)
        assertEquals(20, user.totalIncorrectAnswers)
        assertEquals(5, user.completedTestsCount)
        assertEquals(1, user.achievementsCount)
        assertEquals(1, user.achievements.size)
        assertEquals("https://example.com/avatar.jpg", user.avatarUrl)
    }
    
    @Test
    fun `LoginRequest creation`() {
        val loginRequest = LoginRequest(
            email = "test@example.com",
            password = "password123"
        )
        
        assertEquals("test@example.com", loginRequest.email)
        assertEquals("password123", loginRequest.password)
    }
    
    @Test
    fun `RegisterRequest creation`() {
        val registerRequest = RegisterRequest(
            email = "newuser@example.com",
            password = "newpassword123"
        )
        
        assertEquals("newuser@example.com", registerRequest.email)
        assertEquals("newpassword123", registerRequest.password)
    }
}
