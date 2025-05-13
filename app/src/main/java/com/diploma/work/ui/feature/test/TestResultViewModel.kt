package com.diploma.work.ui.feature.test

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.diploma.work.data.models.TestResult
import com.diploma.work.data.repository.TestsRepository
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
                            _uiState.value = _uiState.value.copy(
                                result = testResult,
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