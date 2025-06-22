package com.diploma.work.ui.feature.test

import androidx.lifecycle.viewModelScope
import com.diploma.work.data.models.Test
import com.diploma.work.data.models.TestSession
import com.diploma.work.data.repository.TestsRepository
import com.diploma.work.ui.base.BaseViewModel
import com.diploma.work.utils.Constants
import com.diploma.work.utils.ErrorHandler
import com.orhanobut.logger.Logger
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TestDetailsUiState(
    val isLoading: Boolean = false,
    val isStartingTest: Boolean = false,
    val error: String? = null,
    val test: Test? = null,
    val testSessionId: String? = null,
    val hasUnfinishedSession: Boolean = false,
    val unfinishedSessionId: String? = null,
    val isCheckingUnfinishedSession: Boolean = false,
    val lastSavedQuestionIndex: Int = 0
)

@HiltViewModel
class TestDetailsViewModel @Inject constructor(
    private val testsRepository: TestsRepository,
    override val errorHandler: ErrorHandler
) : BaseViewModel() {
    private val _uiState = MutableStateFlow(TestDetailsUiState(isLoading = true))
    val uiState: StateFlow<TestDetailsUiState> = _uiState

    private var testId: Long = 0
    private val tag = "TestDetailsVM"

    fun loadTest(id: Long) {
        testId = id
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            checkForUnfinishedSession(id)
            
            testsRepository.getTest(id)
                .catch { e ->
                    Logger.e("$tag: Failed to load test: ${e.message}")
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = e.message ?: "Failed to load test"
                    )
                }
                .collectLatest { result ->
                    result.fold(
                        onSuccess = { test ->
                            Logger.d("$tag: Successfully loaded test: ${test.info.title}")
                            _uiState.value = _uiState.value.copy(
                                test = test,
                                isLoading = false,
                                error = null
                            )
                        },
                        onFailure = { e ->
                            Logger.e("$tag: Failed to load test: ${e.message}")
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                error = e.message ?: "Failed to load test"
                            )
                        }
                    )
                }
        }
    }

    private suspend fun checkForUnfinishedSession(testId: Long) {
        _uiState.value = _uiState.value.copy(isCheckingUnfinishedSession = true)
        try {
            Logger.d("$tag: Checking for unfinished sessions for test ID: $testId")
            val unfinishedSessions = testsRepository.getUncompletedSessions()
            
            val matchingSession = unfinishedSessions.find { it.testId == testId }
            
            if (matchingSession != null) {
                Logger.d("$tag: Found unfinished session: ${matchingSession.sessionId} for test ID: $testId")
                val progress = testsRepository.getSessionProgress(matchingSession.sessionId) ?: 0
                
                _uiState.value = _uiState.value.copy(
                    hasUnfinishedSession = true,
                    unfinishedSessionId = matchingSession.sessionId,
                    lastSavedQuestionIndex = progress,
                    isCheckingUnfinishedSession = false
                )
            } else {
                Logger.d("$tag: No unfinished sessions found for test ID: $testId")
                _uiState.value = _uiState.value.copy(
                    hasUnfinishedSession = false,
                    unfinishedSessionId = null,
                    isCheckingUnfinishedSession = false
                )
            }
        } catch (e: Exception) {
            Logger.e("$tag: Error checking for unfinished sessions: ${e.message}")
            _uiState.value = _uiState.value.copy(
                isCheckingUnfinishedSession = false,
                hasUnfinishedSession = false
            )
        }
    }

    fun startTest() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isStartingTest = true, error = null)
            Logger.d("$tag: Starting new test session for test ID: $testId")
            
            val unfinishedSessionId = _uiState.value.unfinishedSessionId
            if (unfinishedSessionId != null) {
                Logger.d("$tag: Removing unfinished session: $unfinishedSessionId before starting a new one")
                testsRepository.removeUncompletedSession(unfinishedSessionId)
            }
            
            testsRepository.startTestSession(testId)
                .catch { e ->
                    Logger.e("$tag: Failed to start test session: ${e.message}")
                    _uiState.value = _uiState.value.copy(
                        isStartingTest = false,
                        error = e.message ?: "Failed to start test session"
                    )
                }
                .collectLatest { result ->
                    result.fold(
                        onSuccess = { session ->
                            Logger.d("$tag: Successfully started test session: ${session.sessionId}")
                            _uiState.value = _uiState.value.copy(
                                testSessionId = session.sessionId,
                                isStartingTest = false,
                                error = null
                            )
                        },
                        onFailure = { e ->
                            Logger.e("$tag: Failed to start test session: ${e.message}")
                            _uiState.value = _uiState.value.copy(
                                isStartingTest = false,
                                error = e.message ?: "Failed to start test session"
                            )
                        }
                    )
                }
        }
    }
    
    fun continueTest() {
        val sessionId = _uiState.value.unfinishedSessionId ?: return
        Logger.d("$tag: Continuing test session: $sessionId")
        _uiState.value = _uiState.value.copy(testSessionId = sessionId)
    }
}