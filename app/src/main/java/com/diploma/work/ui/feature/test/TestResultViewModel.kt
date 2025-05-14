package com.diploma.work.ui.feature.test

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.diploma.work.data.models.TestResult
import com.diploma.work.data.repository.TestsRepository
import com.orhanobut.logger.Logger
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TestResultUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val result: TestResult? = null
)

@HiltViewModel
class TestResultViewModel @Inject constructor(
    private val testsRepository: TestsRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(TestResultUiState(isLoading = true))
    val uiState: StateFlow<TestResultUiState> = _uiState

    fun loadTestResult(sessionId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            val elapsedTime = testsRepository.getSessionElapsedTime(sessionId) ?: 0L
            Logger.d("TestResultVM: Retrieved elapsed time for session ID: $sessionId - $elapsedTime ms")
            
            testsRepository.getTestResults(sessionId)
                .catch { e ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = e.message ?: "Failed to load test result"
                    )
                }
                .collectLatest { result ->
                    result.fold(
                        onSuccess = { testResult ->
                            val updatedResult = testResult.copy(durationMillis = elapsedTime)
                            Logger.d("TestResultVM: Setting result with duration: ${updatedResult.durationMillis} ms")
                            
                            _uiState.value = _uiState.value.copy(
                                result = updatedResult,
                                isLoading = false,
                                error = null
                            )
                        },
                        onFailure = { e ->
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                error = e.message ?: "Failed to load test result"
                            )
                        }
                    )
                }
        }
    }
}