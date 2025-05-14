package com.diploma.work.ui.feature.test

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.diploma.work.data.models.Answer
import com.diploma.work.data.models.Question
import com.diploma.work.data.models.TestResult
import com.diploma.work.data.models.TestSession
import com.diploma.work.data.repository.TestsRepository
import com.orhanobut.logger.Logger
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class NavigationEvent {
    object NavigateUp : NavigationEvent()
}

data class TestSessionUiState(
    val isLoading: Boolean = false,
    val isSavingAnswer: Boolean = false,
    val isCompletingTest: Boolean = false,
    val error: String? = null,
    val testSession: TestSession? = null,
    val currentQuestionIndex: Int = 0,
    val selectedOptions: List<Int> = emptyList(),
    val textAnswer: String = "",
    val codeAnswer: String = "",
    val showExplanation: Boolean = false,
    val explanationText: String = "",
    val testCompleted: Boolean = false,
    val testResult: TestResult? = null,
    val incorrectlyAnsweredQuestions: Set<Long> = emptySet(),
    val correctlyAnsweredQuestions: Set<Long> = emptySet(),
    val isCurrentQuestionAnswered: Boolean = false,
    val isTestCompletionInProgress: Boolean = false,
    val elapsedTimeMillis: Long = 0
)

@HiltViewModel
class TestSessionViewModel @Inject constructor(
    private val testsRepository: TestsRepository
) : ViewModel() {
    private val tag = "TestSessionVM"
    private val _uiState = MutableStateFlow(TestSessionUiState(isLoading = true))
    val uiState: StateFlow<TestSessionUiState> = _uiState
    
    private var completeTestDeferred: CompletableDeferred<Boolean>? = null
    private var isLeavingTest = false

    private val _navigationEvents = Channel<NavigationEvent>(Channel.BUFFERED)
    val navigationEvents = _navigationEvents.receiveAsFlow()

    init {
        Logger.d("$tag: Initialized")
    }

    private suspend fun sendNavigationEvent(event: NavigationEvent) {
        Logger.d("$tag: Sending navigation event: $event")
        _navigationEvents.send(event)
        Logger.d("$tag: Navigation event sent successfully")
    }

    fun loadTestSession(sessionId: String) {
        Logger.d("$tag: Loading test session with ID: $sessionId")
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            testsRepository.getTestSession(sessionId)
                .catch { e ->
                    Logger.e("$tag: Failed to load test session: ${e.message}")
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = e.message ?: "Failed to load test session"
                    )
                }
                .collectLatest { result ->
                    result.fold(
                        onSuccess = { session ->
                            val currentAnswer = getCurrentAnswer(session)
                            Logger.d("$tag: Test session loaded successfully. Questions: ${session.questions.size}")
                            Logger.v("$tag: Current question index: ${_uiState.value.currentQuestionIndex}")

                            _uiState.value = _uiState.value.copy(
                                testSession = session,
                                isLoading = false,
                                error = null,
                                selectedOptions = currentAnswer?.selectedOptions ?: emptyList(),
                                textAnswer = currentAnswer?.textAnswer ?: "",
                                codeAnswer = currentAnswer?.codeAnswer ?: "",
                                isCurrentQuestionAnswered = isCurrentQuestionAnswered(session)
                            )
                        },
                        onFailure = { e ->
                            Logger.e("$tag: Failed to load test session: ${e.message}")
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                error = e.message ?: "Failed to load test session"
                            )
                        }
                    )
                }
        }
    }

    private fun isCurrentQuestionAnswered(session: TestSession): Boolean {
        val currentQuestion = getCurrentQuestionFromSession(session) ?: return false
        return _uiState.value.incorrectlyAnsweredQuestions.contains(currentQuestion.id) ||
               _uiState.value.correctlyAnsweredQuestions.contains(currentQuestion.id) ||
               session.answers.containsKey(currentQuestion.id)
    }

    private fun getCurrentQuestionFromSession(session: TestSession): Question? {
        val index = _uiState.value.currentQuestionIndex
        if (index >= session.questions.size) return null
        return session.questions[index]
    }

    private fun getCurrentAnswer(session: TestSession): Answer? {
        val currentQuestionIndex = _uiState.value.currentQuestionIndex
        if (currentQuestionIndex >= session.questions.size) return null

        val currentQuestion = session.questions[currentQuestionIndex]
        return session.answers[currentQuestion.id]
    }

    fun getCurrentQuestion(): Question? {
        val session = _uiState.value.testSession ?: return null
        val index = _uiState.value.currentQuestionIndex
        if (index >= session.questions.size) return null
        return session.questions[index]
    }

    fun toggleOption(optionIndex: Int) {
        val currentQuestion = getCurrentQuestion() ?: return
        
        if (_uiState.value.incorrectlyAnsweredQuestions.contains(currentQuestion.id)) {
            Logger.d("$tag: Cannot change answer for incorrectly answered question ID: ${currentQuestion.id}")
            return
        }
        
        val currentOptions = _uiState.value.selectedOptions.toMutableList()

        when (currentQuestion.type) {
            com.diploma.work.data.models.QuestionType.SINGLE_CHOICE,
            com.diploma.work.data.models.QuestionType.CODE -> {
                currentOptions.clear()
                currentOptions.add(optionIndex)
            }
            com.diploma.work.data.models.QuestionType.MULTIPLE_CHOICE -> {
                if (currentOptions.contains(optionIndex)) {
                    currentOptions.remove(optionIndex)
                } else {
                    currentOptions.add(optionIndex)
                }
            }
            else -> {
                return
            }
        }

        _uiState.value = _uiState.value.copy(selectedOptions = currentOptions)
    }

    fun setTextAnswer(text: String) {
        val currentQuestion = getCurrentQuestion() ?: return
        
        if (_uiState.value.incorrectlyAnsweredQuestions.contains(currentQuestion.id)) {
            Logger.d("$tag: Cannot change text answer for incorrectly answered question ID: ${currentQuestion.id}")
            return
        }
        
        Logger.v("$tag: Setting text answer: ${if (text.length > 20) text.take(20) + "..." else text}")
        _uiState.value = _uiState.value.copy(textAnswer = text)
    }

    fun setCodeAnswer(code: String) {
        val currentQuestion = getCurrentQuestion() ?: return
        
        if (_uiState.value.incorrectlyAnsweredQuestions.contains(currentQuestion.id)) {
            Logger.d("$tag: Cannot change code answer for incorrectly answered question ID: ${currentQuestion.id}")
            return
        }
        
        Logger.v("$tag: Setting code answer: ${if (code.length > 20) code.take(20) + "..." else code}")
        _uiState.value = _uiState.value.copy(codeAnswer = code)
    }

    fun saveAnswer() {
        val session = _uiState.value.testSession ?: run {
            Logger.e("$tag: Cannot save answer, session is null")
            return
        }
        val currentQuestion = getCurrentQuestion() ?: run {
            Logger.e("$tag: Cannot save answer, current question is null")
            return
        }
        
        if (_uiState.value.incorrectlyAnsweredQuestions.contains(currentQuestion.id)) {
            Logger.d("$tag: Question was already answered incorrectly, moving to next question")
            moveToNextQuestion(_uiState.value.testSession)
            return
        }
        
        if (_uiState.value.correctlyAnsweredQuestions.contains(currentQuestion.id)) {
            Logger.d("$tag: Question was already answered correctly, moving to next question")
            moveToNextQuestion(_uiState.value.testSession)
            return
        }
        
        val answer = createAnswer(currentQuestion.id)

        Logger.d("$tag: Saving answer for question ID: ${currentQuestion.id}, session ID: ${session.sessionId}")
        Logger.v("$tag: Answer details - Selected options: ${answer.selectedOptions}, " +
                "Has text: ${!answer.textAnswer.isNullOrBlank()}, " +
                "Has code: ${!answer.codeAnswer.isNullOrBlank()}")

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSavingAnswer = true, error = null)
            testsRepository.saveAnswer(session.sessionId, answer)
                .catch { e ->
                    Logger.e("$tag: Failed to save answer: ${e.message}")
                    _uiState.value = _uiState.value.copy(
                        isSavingAnswer = false,
                        error = e.message ?: "Failed to save answer"
                    )
                }
                .collectLatest { result ->
                    result.fold(
                        onSuccess = { success ->
                            if (success) {
                                Logger.d("$tag: Answer saved successfully")
                                val updatedSession = _uiState.value.testSession?.copy(
                                    answers = _uiState.value.testSession!!.answers + (currentQuestion.id to answer)
                                )

                                val isCorrect = isAnswerCorrect(currentQuestion, answer)
                                Logger.d("$tag: Answer is ${if (isCorrect) "correct" else "incorrect"}")

                                if (!isCorrect) {
                                    val updatedIncorrectQuestions = _uiState.value.incorrectlyAnsweredQuestions + currentQuestion.id
                                    
                                    Logger.d("$tag: Showing explanation for incorrect answer")
                                    _uiState.value = _uiState.value.copy(
                                        testSession = updatedSession,
                                        isSavingAnswer = false,
                                        error = null,
                                        showExplanation = true,
                                        explanationText = currentQuestion.explanation,
                                        incorrectlyAnsweredQuestions = updatedIncorrectQuestions,
                                        isCurrentQuestionAnswered = true
                                    )
                                } else {
                                    val updatedCorrectQuestions = _uiState.value.correctlyAnsweredQuestions + currentQuestion.id
                                    
                                    _uiState.value = _uiState.value.copy(
                                        testSession = updatedSession,
                                        isSavingAnswer = false,
                                        error = null,
                                        correctlyAnsweredQuestions = updatedCorrectQuestions,
                                        isCurrentQuestionAnswered = true
                                    )
                                    
                                    moveToNextQuestion(updatedSession)
                                }
                            } else {
                                Logger.e("$tag: Server reported failure saving answer")
                                _uiState.value = _uiState.value.copy(
                                    isSavingAnswer = false,
                                    error = "Failed to save answer"
                                )
                            }
                        },
                        onFailure = { e ->
                            Logger.e("$tag: Exception while saving answer: ${e.message}")
                            _uiState.value = _uiState.value.copy(
                                isSavingAnswer = false,
                                error = e.message ?: "Failed to save answer"
                            )
                        }
                    )
                }
        }
    }

    private fun isAnswerCorrect(question: Question, answer: Answer): Boolean {
        return when (question.type) {
            com.diploma.work.data.models.QuestionType.MULTIPLE_CHOICE -> {
                answer.selectedOptions.sorted() == question.correctOptions.sorted()
            }
            com.diploma.work.data.models.QuestionType.SINGLE_CHOICE,
            com.diploma.work.data.models.QuestionType.CODE -> {
                answer.selectedOptions.firstOrNull() == question.correctOptions.firstOrNull()
            }
            com.diploma.work.data.models.QuestionType.TEXT -> {
                answer.textAnswer?.trim()?.lowercase() == question.correctOptions
                    .firstOrNull()?.toString()?.trim()?.lowercase()
            }
            else -> false
        }
    }

    fun dismissExplanation() {
        Logger.d("$tag: Dismissing explanation")
        val updatedSession = _uiState.value.testSession
        _uiState.value = _uiState.value.copy(
            showExplanation = false,
            explanationText = ""
        )
        if (updatedSession != null) {
            moveToNextQuestion(updatedSession)
        }
    }

    private fun moveToNextQuestion(updatedSession: TestSession?) {
        if (updatedSession == null) {
            Logger.e("$tag: Cannot move to next question, session is null")
            return
        }

        val nextIndex = _uiState.value.currentQuestionIndex + 1
        Logger.d("$tag: Moving to question index: $nextIndex (total: ${updatedSession.questions.size})")

        if (nextIndex >= updatedSession.questions.size) {
            Logger.d("$tag: Reached last question, completing test")
            completeTest()
        } else {
            val nextQuestion = updatedSession.questions[nextIndex]
            val nextAnswer = updatedSession.answers[nextQuestion.id]
            val isNextQuestionAnswered = _uiState.value.incorrectlyAnsweredQuestions.contains(nextQuestion.id) || 
                                        updatedSession.answers.containsKey(nextQuestion.id)
            
            Logger.d("$tag: Moving to next question, ID: ${nextQuestion.id}, already answered: $isNextQuestionAnswered")

            _uiState.value = _uiState.value.copy(
                testSession = updatedSession,
                isSavingAnswer = false,
                currentQuestionIndex = nextIndex,
                selectedOptions = nextAnswer?.selectedOptions ?: emptyList(),
                textAnswer = nextAnswer?.textAnswer ?: "",
                codeAnswer = nextAnswer?.codeAnswer ?: "",
                error = null,
                isCurrentQuestionAnswered = isNextQuestionAnswered
            )
        }
    }

    private fun createAnswer(questionId: Long): Answer {
        return Answer(
            questionId = questionId,
            selectedOptions = _uiState.value.selectedOptions,
            textAnswer = _uiState.value.textAnswer.takeIf { it.isNotBlank() },
            codeAnswer = _uiState.value.codeAnswer.takeIf { it.isNotBlank() }
        )
    }

    fun goToPreviousQuestion() {
        val prevIndex = _uiState.value.currentQuestionIndex - 1
        if (prevIndex >= 0) {
            val session = _uiState.value.testSession ?: return
            val prevQuestion = session.questions[prevIndex]
            val prevAnswer = session.answers[prevQuestion.id]
            val isPrevQuestionAnswered = _uiState.value.incorrectlyAnsweredQuestions.contains(prevQuestion.id) || 
                                       session.answers.containsKey(prevQuestion.id)

            _uiState.value = _uiState.value.copy(
                currentQuestionIndex = prevIndex,
                selectedOptions = prevAnswer?.selectedOptions ?: emptyList(),
                textAnswer = prevAnswer?.textAnswer ?: "",
                codeAnswer = prevAnswer?.codeAnswer ?: "",
                isCurrentQuestionAnswered = isPrevQuestionAnswered
            )
        }
    }

    fun completeTest() {
        if (_uiState.value.isTestCompletionInProgress || _uiState.value.testCompleted) {
            Logger.d("$tag: Test completion already in progress or completed, skipping duplicate request")
            return
        }
        
        if (completeTestDeferred != null && !completeTestDeferred!!.isCompleted) {
            Logger.d("$tag: Complete test request already in progress, skipping duplicate request")
            return
        }
        
        val session = _uiState.value.testSession ?: run {
            Logger.e("$tag: Cannot complete test, session is null")
            return
        }

        val elapsedTime = _uiState.value.elapsedTimeMillis
        Logger.d("$tag: Completing test session: ${session.sessionId} with elapsed time: $elapsedTime ms")

        completeTestDeferred = CompletableDeferred()

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isCompletingTest = true, 
                error = null,
                isTestCompletionInProgress = true
            )
            
            try {
                testsRepository.saveSessionProgress(session.sessionId, _uiState.value.currentQuestionIndex, elapsedTime)
                
                testsRepository.completeTestSession(session.sessionId)
                    .catch { e ->
                        Logger.e("$tag: Failed to complete test: ${e.message}")
                        
                        if (e.message?.contains("session already completed") == true) {
                            Logger.d("$tag: Session already completed, trying to get test results...")
                            _uiState.value = _uiState.value.copy(
                                isCompletingTest = true,
                                error = null,
                                isTestCompletionInProgress = true
                            )
                            
                            loadTestSession(session.sessionId)
                        } else {
                            _uiState.value = _uiState.value.copy(
                                isCompletingTest = false,
                                error = e.message ?: "Failed to complete test",
                                isTestCompletionInProgress = false
                            )
                        }
                        completeTestDeferred?.complete(false)
                    }
                    .collectLatest { result ->
                        result.fold(
                            onSuccess = { testResult ->
                                Logger.d("$tag: Test completed successfully, result score: ${testResult.score}/${testResult.totalPoints}")
                                _uiState.value = _uiState.value.copy(
                                    isCompletingTest = false,
                                    testCompleted = true,
                                    testResult = testResult,
                                    error = null,
                                    isTestCompletionInProgress = false
                                )
                                completeTestDeferred?.complete(true)
                            },
                            onFailure = { e ->
                                Logger.e("$tag: Exception while completing test: ${e.message}")
                                _uiState.value = _uiState.value.copy(
                                    isCompletingTest = false,
                                    error = e.message ?: "Failed to complete test",
                                    isTestCompletionInProgress = false
                                )
                                completeTestDeferred?.complete(false)
                            }
                        )
                    }
            } catch (e: Exception) {
                Logger.e("$tag: Unexpected exception completing test: ${e.message}")
                _uiState.value = _uiState.value.copy(
                    isCompletingTest = false,
                    error = e.message ?: "Unexpected error completing test",
                    isTestCompletionInProgress = false
                )
                completeTestDeferred?.complete(false)
            }
        }
    }

    fun handleLeaveTest() {
        val sessionId = _uiState.value.testSession?.sessionId ?: return
        val isCompletionInProgress = _uiState.value.isTestCompletionInProgress
        val elapsedTime = _uiState.value.elapsedTimeMillis
        
        if (isLeavingTest || isCompletionInProgress) {
            Logger.d("$tag: Already leaving test or completion in progress, navigating up directly")
            viewModelScope.launch {
                sendNavigationEvent(NavigationEvent.NavigateUp)
            }
            return
        }
        
        isLeavingTest = true
        Logger.d("$tag: Handling leave test for session ID: $sessionId")
        
        viewModelScope.launch {
            try {
                val currentQuestion = getCurrentQuestion()
                val currentIndex = _uiState.value.currentQuestionIndex
                
                Logger.d("$tag: Saving session progress at question index: $currentIndex with elapsed time: $elapsedTime ms")
                testsRepository.saveSessionProgress(sessionId, currentIndex, elapsedTime)
                
                if (currentQuestion != null && 
                    !_uiState.value.incorrectlyAnsweredQuestions.contains(currentQuestion.id) &&
                    !_uiState.value.correctlyAnsweredQuestions.contains(currentQuestion.id)) {
                    
                    val answer = createAnswer(currentQuestion.id)
                    Logger.d("$tag: Saving current answer before leaving for question ID: ${currentQuestion.id}")
                    
                    _uiState.value = _uiState.value.copy(isSavingAnswer = true)
                    testsRepository.saveAnswer(sessionId, answer).collect { result ->
                        result.fold(
                            onSuccess = { 
                                Logger.d("$tag: Successfully saved answer before leaving")
                                sendNavigationEvent(NavigationEvent.NavigateUp)
                            },
                            onFailure = { e -> 
                                Logger.e("$tag: Failed to save answer before leaving: ${e.message}")
                                sendNavigationEvent(NavigationEvent.NavigateUp)
                            }
                        )
                    }
                } else {
                    Logger.d("$tag: No answer to save, navigating up directly")
                    sendNavigationEvent(NavigationEvent.NavigateUp)
                }
            } catch (e: Exception) {
                Logger.e("$tag: Error when saving answer before leaving: ${e.message}")
                sendNavigationEvent(NavigationEvent.NavigateUp)
            } finally {
                isLeavingTest = false
                _uiState.value = _uiState.value.copy(isSavingAnswer = false)
            }
        }
    }

    fun loadSavedTestSession(sessionId: String) {
        Logger.d("$tag: Loading saved test session with ID: $sessionId")
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            try {
                val savedProgress = testsRepository.getSessionProgress(sessionId)
                val savedElapsedTime = testsRepository.getSessionElapsedTime(sessionId) ?: 0L
                
                testsRepository.getTestSession(sessionId)
                    .catch { e ->
                        Logger.e("$tag: Failed to load test session: ${e.message}")
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = e.message ?: "Failed to load test session"
                        )
                    }
                    .collectLatest { result ->
                        result.fold(
                            onSuccess = { session ->
                                val savedIndex = savedProgress ?: 0
                                Logger.d("$tag: Restored session progress to question index: $savedIndex with elapsed time: $savedElapsedTime ms")
                                
                                _uiState.value = _uiState.value.copy(
                                    testSession = session,
                                    isLoading = false,
                                    error = null,
                                    currentQuestionIndex = savedIndex,
                                    elapsedTimeMillis = savedElapsedTime
                                )
                                
                                val currentQuestion = session.questions.getOrNull(savedIndex)
                                if (currentQuestion != null) {
                                    val currentAnswer = session.answers[currentQuestion.id]
                                    _uiState.value = _uiState.value.copy(
                                        selectedOptions = currentAnswer?.selectedOptions ?: emptyList(),
                                        textAnswer = currentAnswer?.textAnswer ?: "",
                                        codeAnswer = currentAnswer?.codeAnswer ?: "",
                                        isCurrentQuestionAnswered = isCurrentQuestionAnswered(session)
                                    )
                                }
                            },
                            onFailure = { e ->
                                Logger.e("$tag: Failed to load test session: ${e.message}")
                                _uiState.value = _uiState.value.copy(
                                    isLoading = false,
                                    error = e.message ?: "Failed to load test session"
                                )
                            }
                        )
                    }
            } catch (e: Exception) {
                Logger.e("$tag: Error loading saved session: ${e.message}")
                _uiState.value = _uiState.value.copy(
                    isLoading = false, 
                    error = e.message ?: "Error loading saved session"
                )
            }
        }
    }

    fun updateElapsedTime(timeMillis: Long) {
        _uiState.value = _uiState.value.copy(elapsedTimeMillis = timeMillis)
    }
}