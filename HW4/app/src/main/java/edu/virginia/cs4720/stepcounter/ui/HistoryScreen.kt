package edu.virginia.cs4720.stepcounter.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import edu.virginia.cs4720.stepcounter.data.DailySteps
import edu.virginia.cs4720.stepcounter.data.RETENTION_DAYS
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

private const val CHART_DAYS = 14
private val rowDateFormat = DateTimeFormatter.ofPattern("EEE, MMM d, yyyy", Locale.US)

@Composable
fun HistoryScreen(history: List<DailySteps>, onDeleteAll: () -> Unit) {
    var confirmDelete by remember { mutableStateOf(false) }
    val today = LocalDate.now()

    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        item { ChartCard(history, today) }

        if (history.isEmpty()) {
            item {
                Text(
                    "No history yet. Daily totals appear here once step counting is on.",
                    modifier = Modifier.padding(vertical = 24.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        // history comes from Room already sorted newest first.
        items(history, key = { it.date }) { day ->
            val date = LocalDate.parse(day.date)
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    if (date == today) "Today" else date.format(rowDateFormat),
                    modifier = Modifier.weight(1f),
                    fontWeight = if (date == today) FontWeight.Bold else FontWeight.Normal,
                )
                Text(String.format(Locale.US, "%,d steps", day.steps))
            }
            HorizontalDivider()
        }

        item {
            Column(Modifier.padding(top = 16.dp)) {
                Text(
                    "Only one total per day is stored, on this phone only, and days older than " +
                        "$RETENTION_DAYS days are deleted automatically.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (history.isNotEmpty()) {
                    TextButton(onClick = { confirmDelete = true }) {
                        Text("Delete all history", color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    }

    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text("Delete all history?") },
            text = { Text("This permanently removes every saved daily total.") },
            confirmButton = {
                TextButton(onClick = {
                    confirmDelete = false
                    onDeleteAll()
                }) { Text("Delete") }
            },
            dismissButton = { TextButton(onClick = { confirmDelete = false }) { Text("Cancel") } },
        )
    }
}

/** Bar chart of the last [CHART_DAYS] days, oldest on the left, today on the right. */
@Composable
private fun ChartCard(history: List<DailySteps>, today: LocalDate) {
    val byDate = history.associate { it.date to it.steps }
    val days = (CHART_DAYS - 1 downTo 0).map { today.minusDays(it.toLong()) }
    val values = days.map { byDate[it.toString()] ?: 0L }
    val max = values.maxOrNull()?.coerceAtLeast(1L) ?: 1L

    val barColor = MaterialTheme.colorScheme.primary
    val todayColor = MaterialTheme.colorScheme.tertiary
    val gridColor = MaterialTheme.colorScheme.outlineVariant

    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Text("Last $CHART_DAYS days", style = MaterialTheme.typography.titleMedium)
            Text(
                String.format(Locale.US, "Best day: %,d steps", values.max()),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Canvas(
                Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .padding(top = 12.dp),
            ) {
                val slot = size.width / CHART_DAYS
                val barWidth = slot * 0.6f
                drawLine(gridColor, Offset(0f, size.height), Offset(size.width, size.height), 1.dp.toPx())
                values.forEachIndexed { i, v ->
                    val h = size.height * v / max
                    drawRoundRect(
                        color = if (i == values.lastIndex) todayColor else barColor,
                        topLeft = Offset(i * slot + (slot - barWidth) / 2, size.height - h),
                        size = Size(barWidth, h),
                        cornerRadius = CornerRadius(4.dp.toPx()),
                    )
                }
            }
            Row(Modifier.fillMaxWidth().padding(top = 4.dp)) {
                days.forEach {
                    Text(
                        it.dayOfMonth.toString(),
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}
