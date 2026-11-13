package edu.virginia.cs4720.stepcounter.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import edu.virginia.cs4720.stepcounter.sensors.AccelReading
import edu.virginia.cs4720.stepcounter.sensors.StepDetector
import edu.virginia.cs4720.stepcounter.sensors.StepSource
import java.util.Locale

@Composable
fun LiveScreen(
    reading: AccelReading?,
    hasAccelerometer: Boolean,
    stepSource: StepSource,
    permissionState: PermissionState,
    todaySteps: Long,
    threshold: Float,
    trace: List<Float>,
    onThresholdChange: (Float) -> Unit,
    onRequestPermission: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    Column(
        Modifier
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        AccelerometerCard(reading, hasAccelerometer)
        StepsCard(stepSource, permissionState, todaySteps, onRequestPermission, onOpenSettings)
        if (stepSource == StepSource.ACCELEROMETER && permissionState == PermissionState.GRANTED) {
            TuningCard(threshold, trace, reading?.smoothed, onThresholdChange)
        }
    }
}

@Composable
private fun AccelerometerCard(reading: AccelReading?, hasAccelerometer: Boolean) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Text("Accelerometer (m/s²)", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(12.dp))
            if (!hasAccelerometer) {
                Text("This device has no accelerometer.")
                return@Column
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                AxisValue("x", reading?.x)
                AxisValue("y", reading?.y)
                AxisValue("z", reading?.z)
            }
        }
    }
}

@Composable
private fun AxisValue(label: String, value: Float?) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(
            text = value?.let { String.format(Locale.US, "%6.2f", it) } ?: "  –  ",
            style = MaterialTheme.typography.headlineSmall,
            fontFamily = FontFamily.Monospace,
        )
    }
}

@Composable
private fun StepsCard(
    stepSource: StepSource,
    permissionState: PermissionState,
    todaySteps: Long,
    onRequestPermission: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Steps today", style = MaterialTheme.typography.titleMedium)
            when {
                stepSource == StepSource.NONE -> Text(
                    "Step counting isn't available: this device has neither a step counter nor an accelerometer.",
                )

                permissionState == PermissionState.NOT_ASKED -> {
                    Text(
                        "To count your steps, StepCounter needs access to your physical activity; " +
                            "it only saves one step total per day, on this phone.",
                    )
                    Button(onClick = onRequestPermission) { Text("Start counting steps") }
                }

                permissionState == PermissionState.DENIED -> {
                    Text(
                        "Step counting is off because physical activity access was not allowed. " +
                            "The live accelerometer above still works.",
                    )
                    Text(
                        "To turn it on later, open Settings › Apps › StepCounter › Permissions › " +
                            "Physical activity, and choose Allow.",
                        style = MaterialTheme.typography.bodySmall,
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(onClick = onOpenSettings) { Text("Open settings") }
                        OutlinedButton(onClick = onRequestPermission) { Text("Ask again") }
                    }
                }

                else -> {
                    Text(
                        String.format(Locale.US, "%,d", todaySteps),
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        when (stepSource) {
                            StepSource.HARDWARE -> "Source: hardware step counter"
                            else -> "Source: accelerometer peak detection (counts only while the app is open)"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
private fun TuningCard(
    threshold: Float,
    trace: List<Float>,
    smoothed: Float?,
    onThresholdChange: (Float) -> Unit,
) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Step detector", style = MaterialTheme.typography.titleMedium)
            Text(
                "A step is counted when the smoothed magnitude (line) peaks above the threshold " +
                    "(dashed), at least 0.3 s after the previous step.",
                style = MaterialTheme.typography.bodySmall,
            )
            MagnitudeTrace(trace, threshold)
            Text(
                String.format(
                    Locale.US,
                    "Smoothed magnitude: %5.2f m/s²\nThreshold:          %5.1f m/s²",
                    smoothed ?: 0f,
                    threshold,
                ),
                fontFamily = FontFamily.Monospace,
                style = MaterialTheme.typography.bodySmall,
            )
            Slider(
                value = threshold,
                onValueChange = { onThresholdChange((it * 10).toInt() / 10f) },
                valueRange = StepDetector.MIN_THRESHOLD..StepDetector.MAX_THRESHOLD,
            )
        }
    }
}

@Composable
private fun MagnitudeTrace(trace: List<Float>, threshold: Float) {
    val lineColor = MaterialTheme.colorScheme.primary
    val thresholdColor = MaterialTheme.colorScheme.error
    val minY = 8f
    val maxY = maxOf(threshold + 3f, trace.maxOrNull() ?: 0f)

    Canvas(
        Modifier
            .fillMaxWidth()
            .height(120.dp),
    ) {
        fun yFor(v: Float) = size.height * (1f - ((v - minY) / (maxY - minY)).coerceIn(0f, 1f))

        val ty = yFor(threshold)
        drawLine(
            color = thresholdColor,
            start = Offset(0f, ty),
            end = Offset(size.width, ty),
            strokeWidth = 2.dp.toPx(),
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f)),
        )
        if (trace.size < 2) return@Canvas
        val step = size.width / (trace.size - 1)
        val path = Path().apply {
            moveTo(0f, yFor(trace[0]))
            for (i in 1 until trace.size) lineTo(i * step, yFor(trace[i]))
        }
        drawPath(path, lineColor, style = Stroke(width = 2.dp.toPx()))
    }
}
