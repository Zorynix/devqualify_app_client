package com.diploma.work.ui.feature.test

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.diploma.work.data.models.Answer
import com.diploma.work.data.models.Question
import com.diploma.work.data.models.TestResult
import com.diploma.work.data.models.TestSession
import com.diploma.work.data.repository.TestsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

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
    val testResult: TestResult? = null
)

@HiltViewModel
class TestSessionViewModel @Inject constructor(
    private val testsRepository: TestsRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(TestSessionUiState(isLoading = true))
    val uiState: StateFlow<TestSessionUiState> = _uiState

    fun loadTestSession(sessionId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            testsRepository.getTestSession(sessionId)
                .catch { e ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = e.message ?: "Failed to load test session"
                    )
                }
                .collectLatest { result ->
                    result.fold(
                        onSuccess = { session ->
                            val currentAnswer = getCurrentAnswer(session)
                            _uiState.value = _uiState.value.copy(
                                testSession = session,
                                isLoading = false,
                                error = null,
                                selectedOptions = currentAnswer?.selectedOptions ?: emptyList(),
                                textAnswer = currentAnswer?.textAnswer ?: "",
                                codeAnswer = currentAnswer?.codeAnswer ?: ""
                            )
                        },
                        onFailure = { e ->
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                error = e.message ?: "Failed to load test session"
                            )
                        }
                    )
                }
        }
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
        val currentOptions = _uiState.value.selectedOptions.toMutableList()
        if (currentOptions.contains(optionIndex)) {
            currentOptions.remove(optionIndex)
        } else {
            currentOptions.add(optionIndex)
        }
        _uiState.value = _uiState.value.copy(selectedOptions = currentOptions)
    }

    fun setTextAnswer(text: String) {
        _uiState.value = _uiState.value.copy(textAnswer = text)
    }

    fun setCodeAnswer(code: String) {
        _uiState.value = _uiState.value.copy(codeAnswer = code)
    }

    fun saveAnswer() {
        val session = _uiState.value.testSession ?: return
        val currentQuestion = getCurrentQuestion() ?: return
        val answer = createAnswer(currentQuestion.id)

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSavingAnswer = true, error = null)
            testsRepository.saveAnswer(session.sessionId, answer)
                .catch { e ->
                    _uiState.value = _uiState.value.copy(
                        isSavingAnswer = false,
                        error = e.message ?: "Failed to save answer"
                    )
                }
                .collectLatest { result ->
                    result.fold(
                        onSuccess = { success ->
                            if (success) {
                                val updatedSession = _uiState.value.testSession?.copy(
                                    answers = _uiState.value.testSession!!.answers + (currentQuestion.id to answer)
                                )
                                
                                val isCorrect = isAnswerCorrect(currentQuestion, answer)
                                if (!isCorrect) {
                                    _uiState.value = _uiState.value.copy(
                                        testSession = updatedSession,
                                        isSavingAnswer = false,
                                        error = null,
                                        showExplanation = true,
                                        explanationText = currentQuestion.explanation
                                    )
                                } else {
                                    moveToNextQuestion(updatedSession)
                                }
                            } else {
                                _uiState.value = _uiState.value.copy(
                                    isSavingAnswer = false,
                                    error = "Failed to save answer"
                                )
                            }
                        },
                        onFailure = { e ->
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
            com.diploma.work.data.models.QuestionType.MCQ -> {
                answer.selectedOptions.sorted() == question.correctOptions.sorted()
            }
            com.diploma.work.data.models.QuestionType.TEXT -> {
                answer.textAnswer?.trim()?.lowercase() == question.correctOptions
                    .firstOrNull()?.toString()?.trim()?.lowercase()
            }
            com.diploma.work.data.models.QuestionType.CODE -> {
                true
            }
            else -> false
        }
    }

    fun dismissExplanation() {
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
        if (updatedSession == null) return
        
        val nextIndex = _uiState.value.currentQuestionIndex + 1
        if (nextIndex >= updatedSession.questions.size) {
            completeTest()
        } else {
            val nextAnswer = updatedSession.answers[updatedSession.questions[nextIndex].id]
            _uiState.value = _uiState.value.copy(
                testSession = updatedSession,
                isSavingAnswer = false,
                currentQuestionIndex = nextIndex,
                selectedOptions = nextAnswer?.selectedOptions ?: emptyList(),
                textAnswer = nextAnswer?.textAnswer ?: "",
                codeAnswer = nextAnswer?.codeAnswer ?: "",
                error = null
            )
        }
    }

    private fun createAnswer(questionId: Long): Answer {
        return Answer(
            questionId = questionId,
            selectedOptions = _uiState.value.selectedOptions,
            textAnswer = if (_uiState.value.textAnswer.isNotBlank()) _uiState.value.textAnswer else null,
            codeAnswer = if (_uiState.value.codeAnswer.isNotBlank()) _uiState.value.codeAnswer else null
        )
    }

    fun goToPreviousQuestion() {
        val prevIndex = _uiState.value.currentQuestionIndex - 1
        if (prevIndex >= 0) {
            val session = _uiState.value.testSession ?: return
            val prevQuestion = session.questions[prevIndex]
            val prevAnswer = session.answers[prevQuestion.id]
            
            _uiState.value = _uiState.value.copy(
                currentQuestionIndex = prevIndex,
                selectedOptions = prevAnswer?.selectedOptions ?: emptyList(),
                textAnswer = prevAnswer?.textAnswer ?: "",
                codeAnswer = prevAnswer?.codeAnswer ?: ""
            )
        }
    }

    fun completeTest() {
        val session = _uiState.value.testSession ?: return
        
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isCompletingTest = true, error = null)
            testsRepository.completeTestSession(session.sessionId)
                .catch { e ->
                    _uiState.value = _uiState.value.copy(
                        isCompletingTest = false,
                        error = e.message ?: "Failed to complete test"
                    )
                }
                .collectLatest { result ->
                    result.fold(
                        onSuccess = { testResult ->
                            _uiState.value = _uiState.value.copy(
                                isCompletingTest = false,
                                testCompleted = true,
                                testResult = testResult,
                                error = null
                            )
                        },
                        onFailure = { e ->
                            _uiState.value = _uiState.value.copy(
                                isCompletingTest = false,
                                error = e.message ?: "Failed to complete test"
                            )
                        }
                    )
                }
        }
    }
} 