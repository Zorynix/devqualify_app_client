package com.diploma.work.ui.feature.home

import androidx.lifecycle.viewModelScope
import com.diploma.work.data.models.Direction
import com.diploma.work.data.models.Level
import com.diploma.work.data.models.Technology
import com.diploma.work.data.models.TestInfo
import com.diploma.work.data.repository.TestsRepository
import com.diploma.work.ui.base.BaseViewModel
import com.diploma.work.utils.Constants
import com.diploma.work.utils.ErrorHandler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val error: String? = null,
    val tests: List<TestInfo> = emptyList(),
    val technologies: List<Technology> = emptyList(),
    val selectedTechnology: Technology? = null,
    val selectedDirection: Direction? = null,
    val selectedLevel: Level? = null
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val testsRepository: TestsRepository,
    override val errorHandler: ErrorHandler
) : BaseViewModel() {private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState

    init {
        setLoading(true)
        loadTests()
        loadTechnologies()
    }

    fun loadTests() {
        viewModelScope.launch {
            setLoading(true)
            clearGlobalError()

            try {                val direction = _uiState.value.selectedDirection
                val level = _uiState.value.selectedLevel
                val technologyId = _uiState.value.selectedTechnology?.id

                val testFlow = if (technologyId != null) {
                    testsRepository.getTestsByTechnology(technologyId, level)
                } else {
                    testsRepository.getTests(direction, level)
                }

                testFlow
                    .catch { e ->
                        val errorMessage = errorHandler.handleError(e, Constants.ErrorMessages.NETWORK_ERROR)
                        setError(errorMessage)
                        setLoading(false)
                    }
                    .collectLatest { result ->
                        result.fold(
                            onSuccess = { tests ->
                                _uiState.value = _uiState.value.copy(
                                    tests = tests,
                                    error = null
                                )
                                setLoading(false)
                            },
                            onFailure = { e ->
                                val errorMessage = errorHandler.handleError(e, Constants.ErrorMessages.NETWORK_ERROR)
                                setError(errorMessage)
                                setLoading(false)
                            }
                        )
                    }            } catch (e: Exception) {
                val errorMessage = errorHandler.handleError(e, Constants.ErrorMessages.GENERIC_ERROR)
                setError(errorMessage)
                setLoading(false)
            }
        }
    }private fun loadTechnologies() {
        viewModelScope.launch {
            try {
                testsRepository.getTechnologies()
                    .catch { e ->
                        val errorMessage = errorHandler.handleError(e, Constants.ErrorMessages.NETWORK_ERROR)
                        setError(errorMessage)
                    }
                    .collectLatest { result ->
                        result.fold(
                            onSuccess = { technologies ->
                                _uiState.value = _uiState.value.copy(
                                    technologies = technologies
                                )
                            },
                            onFailure = { e ->
                                val errorMessage = errorHandler.handleError(e, Constants.ErrorMessages.NETWORK_ERROR)
                                setError(errorMessage)
                            }
                        )
                    }
            } catch (e: Exception) {
                val errorMessage = errorHandler.handleError(e, Constants.ErrorMessages.GENERIC_ERROR)
                setError(errorMessage)
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