package com.diploma.work.ui.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.diploma.work.data.models.Direction
import com.diploma.work.data.models.Level
import com.diploma.work.data.models.Technology
import com.diploma.work.data.models.TestInfo
import com.diploma.work.data.repository.TestsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val tests: List<TestInfo> = emptyList(),
    val technologies: List<Technology> = emptyList(),
    val selectedTechnology: Technology? = null,
    val selectedDirection: Direction? = null,
    val selectedLevel: Level? = null
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val testsRepository: TestsRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(HomeUiState(isLoading = true))
    val uiState: StateFlow<HomeUiState> = _uiState

    init {
        loadTests()
        loadTechnologies()
    }

    fun loadTests() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            val direction = _uiState.value.selectedDirection
            val level = _uiState.value.selectedLevel
            val technologyId = _uiState.value.selectedTechnology?.id
            
            val testFlow = if (technologyId != null) {
                testsRepository.getTestsByTechnology(technologyId, level)
            } else {
                testsRepository.getTests(direction, level)
            }
            
            testFlow
                .catch { e ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = e.message ?: "Failed to load tests"
                    )
                }
                .collectLatest { result ->
                    result.fold(
                        onSuccess = { tests ->
                            _uiState.value = _uiState.value.copy(
                                tests = tests,
                                isLoading = false,
                                error = null
                            )
                        },
                        onFailure = { e ->
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                error = e.message ?: "Failed to load tests"
                            )
                        }
                    )
                }
        }
    }

    private fun loadTechnologies() {
        viewModelScope.launch {
            testsRepository.getTechnologies()
                .catch { e ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = e.message ?: "Failed to load technologies"
                    )
                }
                .collectLatest { result ->
                    result.fold(
                        onSuccess = { technologies ->
                            _uiState.value = _uiState.value.copy(
                                technologies = technologies,
                                isLoading = false
                            )
                        },
                        onFailure = { e ->
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                error = e.message ?: "Failed to load technologies"
                            )
                        }
                    )
                }
        }
    }

    fun selectDirection(direction: Direction?) {
        _uiState.value = _uiState.value.copy(selectedDirection = direction)
        loadTests()
    }

    fun selectLevel(level: Level?) {
        _uiState.value = _uiState.value.copy(selectedLevel = level)
        loadTests()
    }

    fun selectTechnology(technology: Technology?) {
        _uiState.value = _uiState.value.copy(selectedTechnology = technology)
        loadTests()
    }
}