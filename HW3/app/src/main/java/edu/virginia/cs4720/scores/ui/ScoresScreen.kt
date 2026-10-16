package edu.virginia.cs4720.scores.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import edu.virginia.cs4720.scores.data.GameStatus
import edu.virginia.cs4720.scores.data.local.GameEntity
import edu.virginia.cs4720.scores.data.status
import edu.virginia.cs4720.scores.data.statusText
import androidx.compose.material3.TextButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScoresScreen(vm: ScoresViewModel) {
    val state by vm.uiState.collectAsState()
    val selection by vm.selection.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("College Football") },
                actions = {
                    TextButton(onClick = vm::refresh) {
                        Text("Refresh")
                    }
                },
            )
        }
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            DivisionToggle(selection.division, vm::selectDivision)
            WeekSelector(selection.week, vm::selectWeek)

            Box(Modifier.fillMaxWidth().height(4.dp)) {
                if ((state as? UiState.Success)?.isRefreshing == true) {
                    LinearProgressIndicator(Modifier.fillMaxWidth())
                }
            }

            when (val s = state) {
                is UiState.Loading ->
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                is UiState.Error -> ErrorView(s.message, onRetry = vm::refresh)
                is UiState.Success ->
                    if (s.games.isEmpty()) EmptyView() else GameList(s)
            }
        }
    }
}

@Composable
fun DivisionToggle(selected: String, onSelect: (String) -> Unit) {
    Row(
        Modifier.padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        listOf("fbs", "fcs").forEach { d ->
            FilterChip(
                selected = selected == d,
                onClick = { onSelect(d) },
                label = { Text(d.uppercase()) },
            )
        }
    }
}

@Composable
fun WeekSelector(selected: Int, onSelect: (Int) -> Unit) {
    val listState = rememberLazyListState()
    LaunchedEffect(Unit) { listState.scrollToItem((selected - 2).coerceAtLeast(0)) }

    LazyRow(
        state = listState,
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items((1..14).toList()) { w ->
            FilterChip(
                selected = w == selected,
                onClick = { onSelect(w) },
                label = { Text("Wk $w") },
            )
        }
    }
}

@Composable
fun GameList(s: UiState.Success) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        if (s.refreshFailed) item { OfflineBanner() }
        items(s.games, key = { it.gameId }) { GameRow(it) }
    }
}

@Composable
fun OfflineBanner() {
    Surface(
        color = MaterialTheme.colorScheme.secondaryContainer,
        shape = MaterialTheme.shapes.small,
    ) {
        Text(
            "Offline. Showing saved scores.",
            Modifier.padding(12.dp).fillMaxWidth(),
        )
    }
}

@Composable
fun GameRow(g: GameEntity) {
    val status = g.status()
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Text(
                g.statusText(),
                style = MaterialTheme.typography.labelLarge,
                color = if (status == GameStatus.LIVE) MaterialTheme.colorScheme.error
                else MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(8.dp))
            TeamLine(g.awayName, "AWAY", g.awayScore, g.awayWinner && status == GameStatus.FINAL, status)
            TeamLine(g.homeName, "HOME", g.homeScore, g.homeWinner && status == GameStatus.FINAL, status)
        }
    }
}

@Composable
fun TeamLine(name: String, role: String, score: Int?, winner: Boolean, status: GameStatus) {
    Row(
        Modifier.fillMaxWidth().padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(name, fontWeight = if (winner) FontWeight.Bold else FontWeight.Normal)
            Text(role, style = MaterialTheme.typography.labelSmall)
        }
        if (winner) Text("✔ ", fontWeight = FontWeight.Bold)
        if (status != GameStatus.UPCOMING) {
            Text(
                score?.toString() ?: "–",
                fontWeight = if (winner) FontWeight.Bold else FontWeight.Normal,
            )
        }
    }
}

@Composable
fun ErrorView(message: String, onRetry: () -> Unit) {
    Column(
        Modifier.fillMaxSize().padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text("⚠️", style = MaterialTheme.typography.displayMedium)
        Spacer(Modifier.height(12.dp))
        Text(message, textAlign = TextAlign.Center)
        Spacer(Modifier.height(16.dp))
        Button(onClick = onRetry) { Text("Retry") }
    }
}

@Composable
fun EmptyView() {
    Column(
        Modifier.fillMaxSize().padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text("🏈", style = MaterialTheme.typography.displayMedium)
        Spacer(Modifier.height(12.dp))
        Text("No games this week", style = MaterialTheme.typography.titleMedium)
        Text("Try another week.", color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}