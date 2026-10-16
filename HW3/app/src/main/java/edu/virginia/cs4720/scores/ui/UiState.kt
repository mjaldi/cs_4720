package edu.virginia.cs4720.scores.ui

import edu.virginia.cs4720.scores.data.local.GameEntity

sealed interface UiState {
    data object Loading : UiState

    data class Success(
        val games: List<GameEntity>,
        val isRefreshing: Boolean,
        val refreshFailed: Boolean,
    ) : UiState

    data class Error(val message: String) : UiState
}