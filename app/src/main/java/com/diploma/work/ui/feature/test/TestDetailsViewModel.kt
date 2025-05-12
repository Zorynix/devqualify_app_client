package com.diploma.work.ui.feature.test

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.diploma.work.data.models.Test
import com.diploma.work.data.repository.TestsRepository
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
    val testSessionId: String? = null
)

@HiltViewModel
class TestDetailsViewModel @Inject constructor(
    private val testsRepository: TestsRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(TestDetailsUiState(isLoading = true))
    val uiState: StateFlow<TestDetailsUiState> = _uiState

    private var testId: Long = 0

    fun loadTest(id: Long) {
        testId = id
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            testsRepository.getTest(id)
                .catch { e ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = e.message ?: "Failed to load test"
                    )
                }
                .collectLatest { result ->
                    result.fold(
                        onSuccess = { test ->
                            _uiState.value = _uiState.value.copy(
                                test = test,
                                isLoading = false,
                                error = null
                            )
                        },
                        onFailure = { e ->
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                error = e.message ?: "Failed to load test"
                            )
                        }
                    )
                }
        }
    }

    fun startTest() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isStartingTest = true, error = null)
            testsRepository.startTestSession(testId)
                .catch { e ->
                    _uiState.value = _uiState.value.copy(
                        isStartingTest = false,
                        error = e.message ?: "Failed to start test session"
                    )
                }
                .collectLatest { result ->
                    result.fold(
                        onSuccess = { session ->
                            _uiState.value = _uiState.value.copy(
                                testSessionId = session.sessionId,
                                isStartingTest = false,
                                error = null
                            )
                        },
                        onFailure = { e ->
                            _uiState.value = _uiState.value.copy(
                                isStartingTest = false,
                                error = e.message ?: "Failed to start test session"
                            )
                        }
                    )
                }
        }
    }
} 