package com.diploma.work.ui.feature.leaderboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.diploma.work.data.AppSession
import com.diploma.work.data.models.GetLeaderboardRequest
import com.diploma.work.data.models.GetLeaderboardResponse
import com.diploma.work.data.models.GetUserRequest
import com.diploma.work.data.models.GetUserResponse
import com.diploma.work.data.models.Pagination
import com.diploma.work.data.models.User
import com.diploma.work.data.repository.UserInfoRepository
import com.diploma.work.grpc.Direction
import com.diploma.work.grpc.Level
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LeaderboardUIState(
    val users: List<User> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val direction: Direction = Direction.DIRECTION_UNSPECIFIED,
    val level: Level = Level.LEVEL_UNSPECIFIED,
    val sortType: LeaderboardSortType = LeaderboardSortType.ACHIEVEMENTS,
    val nextPageToken: String = "",
    val hasMoreData: Boolean = false,
    val selectedUser: User? = null,
    val isUserDetailDialogVisible: Boolean = false
)

enum class LeaderboardSortType(val displayName: String) {
    ACHIEVEMENTS("Achievements"),
    CORRECT_ANSWERS("Correct Answers"),
    COMPLETED_TESTS("Completed Tests")
}

@HiltViewModel
class LeaderboardViewModel @Inject constructor(
    private val userInfoRepository: UserInfoRepository,
    val session: AppSession
) : ViewModel() {

    private val _uiState = MutableStateFlow(LeaderboardUIState())
    val uiState: StateFlow<LeaderboardUIState> = _uiState.asStateFlow()

    init {
        loadLeaderboard()
    }

    fun loadLeaderboard(isRefreshing: Boolean = false) {
        if (isRefreshing) {
            _uiState.value = _uiState.value.copy(
                nextPageToken = "",
                users = emptyList(),
                hasMoreData = false
            )
        }

        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
        
        viewModelScope.launch {
            val request = GetLeaderboardRequest(
                direction = _uiState.value.direction,
                level = _uiState.value.level,
                pagination = Pagination(PAGE_SIZE, _uiState.value.nextPageToken)
            )
            
            userInfoRepository.getLeaderboard(request)
                .onSuccess { response ->
                    val sortedUsers = sortUsersByCriteria(
                        if (isRefreshing) response.users else _uiState.value.users + response.users, 
                        _uiState.value.sortType
                    )
                    
                    _uiState.value = _uiState.value.copy(
                        users = sortedUsers,
                        isLoading = false,
                        nextPageToken = response.nextPageToken,
                        hasMoreData = response.nextPageToken.isNotEmpty()
                    )
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = error.localizedMessage ?: "Failed to load leaderboard"
                    )
                }
        }
    }

    fun loadMoreUsers() {
        if (_uiState.value.isLoading || _uiState.value.nextPageToken.isEmpty()) return
        
        loadLeaderboard()
    }

    fun setDirection(direction: Direction) {
        if (_uiState.value.direction == direction) return
        
        _uiState.value = _uiState.value.copy(direction = direction)
        loadLeaderboard(true)
    }

    fun setLevel(level: Level) {
        if (_uiState.value.level == level) return
        
        _uiState.value = _uiState.value.copy(level = level)
        loadLeaderboard(true)
    }

    fun setSortType(sortType: LeaderboardSortType) {
        if (_uiState.value.sortType == sortType) return
        
        val sortedUsers = sortUsersByCriteria(_uiState.value.users, sortType)
        
        _uiState.value = _uiState.value.copy(
            sortType = sortType,
            users = sortedUsers
        )
    }

    fun selectUser(user: User) {
        _uiState.value = _uiState.value.copy(
            selectedUser = user,
            isUserDetailDialogVisible = true
        )
    }

    fun dismissUserDetailDialog() {
        _uiState.value = _uiState.value.copy(
            isUserDetailDialogVisible = false
        )
    }

    fun getUserById(userId: Long) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            userInfoRepository.getUser(GetUserRequest(userId))
                .onSuccess { response ->
                    _uiState.value = _uiState.value.copy(
                        selectedUser = response.user,
                        isLoading = false,
                        isUserDetailDialogVisible = true
                    )
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = error.localizedMessage ?: "Failed to load user details"
                    )
                }
        }
    }
    
    private fun sortUsersByCriteria(users: List<User>, sortType: LeaderboardSortType): List<User> {
        return when (sortType) {
            LeaderboardSortType.ACHIEVEMENTS -> users.sortedByDescending { it.achievementsCount }
            LeaderboardSortType.CORRECT_ANSWERS -> users.sortedByDescending { it.totalCorrectAnswers }
            LeaderboardSortType.COMPLETED_TESTS -> users.sortedByDescending { it.completedTestsCount }
        }
    }
    
    companion object {
        private const val PAGE_SIZE = 20
    }
}
