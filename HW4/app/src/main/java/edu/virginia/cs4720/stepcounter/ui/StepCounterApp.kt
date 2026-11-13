package edu.virginia.cs4720.stepcounter.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import edu.virginia.cs4720.stepcounter.data.StepRepository
import edu.virginia.cs4720.stepcounter.sensors.SensorController
import kotlinx.coroutines.launch
import java.time.LocalDate

enum class PermissionState { NOT_ASKED, GRANTED, DENIED }

@Composable
fun StepCounterApp(
    sensors: SensorController,
    repo: StepRepository,
    permissionState: PermissionState,
    onRequestPermission: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    var tab by rememberSaveable { mutableIntStateOf(0) }
    val reading by sensors.reading.collectAsState()
    val trace by sensors.trace.collectAsState()
    val history by repo.history.collectAsState(initial = emptyList())
    var threshold by remember { mutableFloatStateOf(sensors.threshold) }
    val scope = rememberCoroutineScope()

    val today = LocalDate.now().toString()
    val todaySteps = history.firstOrNull { it.date == today }?.steps ?: 0L

    Scaffold(
        topBar = {
            PrimaryTabRow(selectedTabIndex = tab, modifier = Modifier.statusBarsPadding()) {
                Tab(selected = tab == 0, onClick = { tab = 0 }, text = { Text("Live") })
                Tab(selected = tab == 1, onClick = { tab = 1 }, text = { Text("History") })
            }
        },
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            when (tab) {
                0 -> LiveScreen(
                    reading = reading,
                    hasAccelerometer = sensors.hasAccelerometer,
                    stepSource = sensors.stepSource,
                    permissionState = permissionState,
                    todaySteps = todaySteps,
                    threshold = threshold,
                    trace = trace,
                    onThresholdChange = {
                        threshold = it
                        sensors.threshold = it
                    },
                    onRequestPermission = onRequestPermission,
                    onOpenSettings = onOpenSettings,
                )

                else -> HistoryScreen(
                    history = history,
                    onDeleteAll = { scope.launch { repo.deleteAllHistory() } },
                )
            }
        }
    }
}
