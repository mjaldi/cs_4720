package edu.virginia.cs4720.scores.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import edu.virginia.cs4720.scores.data.GameRepository
import edu.virginia.cs4720.scores.data.defaultWeek
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class Selection(
    val division: String = "fbs",
    val week: Int,
    val nonce: Int = 0,
)

private enum class Fetch { LOADING, OK, FAILED }

@OptIn(ExperimentalCoroutinesApi::class)
class ScoresViewModel(
    private val repo: GameRepository,
    private val season: Int = 2026,
) : ViewModel() {

    private val _selection = MutableStateFlow(Selection(week = defaultWeek()))
    val selection: StateFlow<Selection> = _selection.asStateFlow()

    private val fetch = MutableStateFlow(Fetch.LOADING)

    init {
        viewModelScope.launch {
            _selection.collectLatest { s ->
                val ok = repo.refresh(s.division, season, s.week).isSuccess
                fetch.value = if (ok) Fetch.OK else Fetch.FAILED
            }
        }
    }

    val uiState: StateFlow<UiState> = combine(
        _selection
            .map { it.division to it.week }
            .distinctUntilChanged()
            .flatMapLatest { (d, w) -> repo.observe(d, season, w) },
        fetch,
    ) { games, f ->
        when {
            games.isNotEmpty() -> UiState.Success(games, f == Fetch.LOADING, f == Fetch.FAILED)
            f == Fetch.LOADING -> UiState.Loading
            f == Fetch.FAILED -> UiState.Error("Couldn't load scores. Check your connection.")
            else -> UiState.Success(emptyList(), false, false)   // fetched fine, week has no games
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), UiState.Loading)

    private fun change(transform: (Selection) -> Selection) {
        val old = _selection.value
        val new = transform(old)
        if (new == old) return
        fetch.value = Fetch.LOADING   // set BEFORE the selection changes so the empty state doesn't flash
        _selection.value = new
    }

    fun selectWeek(week: Int) = change { it.copy(week = week) }
    fun selectDivision(division: String) = change { it.copy(division = division) }
    fun refresh() = change { it.copy(nonce = it.nonce + 1) }
}

class ScoresViewModelFactory(private val repo: GameRepository) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T = ScoresViewModel(repo) as T
}