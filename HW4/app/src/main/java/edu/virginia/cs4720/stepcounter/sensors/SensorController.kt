package edu.virginia.cs4720.stepcounter.sensors

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import edu.virginia.cs4720.stepcounter.data.StepRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class StepSource { HARDWARE, ACCELEROMETER, NONE }

data class AccelReading(val x: Float, val y: Float, val z: Float, val smoothed: Float)

/**
 * Owns the sensor listeners. The Activity calls [start] from onResume and [stop]
 * from onPause, so nothing keeps listening once the user leaves the app.
 *
 * [scope] should run on the main thread so that hardware-counter readings are
 * processed one at a time, in order.
 */
class SensorController(
    context: Context,
    private val scope: CoroutineScope,
    private val repo: StepRepository,
) : SensorEventListener {

    private val sensorManager: SensorManager? = context.getSystemService(SensorManager::class.java)
    private val accelerometer: Sensor? = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    private val stepCounter: Sensor? = sensorManager?.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)

    /** Chosen once, at startup, from what this device actually has. */
    val stepSource: StepSource = when {
        stepCounter != null -> StepSource.HARDWARE
        accelerometer != null -> StepSource.ACCELEROMETER
        else -> StepSource.NONE
    }
    val hasAccelerometer: Boolean get() = accelerometer != null

    private val detector = StepDetector(threshold = repo.threshold)
    private var countingSteps = false

    private val _reading = MutableStateFlow<AccelReading?>(null)
    val reading: StateFlow<AccelReading?> = _reading.asStateFlow()

    /** Recent smoothed magnitudes (~3 s at 50 Hz) for the tuning graph. */
    private val traceBuffer = ArrayDeque<Float>()
    private val _trace = MutableStateFlow<List<Float>>(emptyList())
    val trace: StateFlow<List<Float>> = _trace.asStateFlow()

    var threshold: Float
        get() = detector.threshold
        set(value) {
            detector.threshold = value
            repo.threshold = value
        }

    /** Registers listeners. Safe to call again; it unregisters first. */
    fun start(countSteps: Boolean) {
        stop()
        val sm = sensorManager ?: return
        countingSteps = countSteps && stepSource != StepSource.NONE

        // Detecting steps from the accelerometer needs ~50 Hz to see each peak clearly.
        // When we only display the values, ~16 Hz is plenty and costs less battery.
        accelerometer?.let {
            val rate = if (countingSteps && stepSource == StepSource.ACCELEROMETER) {
                SensorManager.SENSOR_DELAY_GAME
            } else {
                SensorManager.SENSOR_DELAY_UI
            }
            sm.registerListener(this, it, rate)
        }
        if (countingSteps && stepSource == StepSource.HARDWARE) {
            sm.registerListener(this, stepCounter, SensorManager.SENSOR_DELAY_NORMAL)
        }
        if (countingSteps) scope.launch { repo.startDay() }
    }

    fun stop() {
        sensorManager?.unregisterListener(this)
        countingSteps = false
        detector.reset()
        traceBuffer.clear()
    }

    override fun onSensorChanged(event: SensorEvent) {
        when (event.sensor.type) {
            Sensor.TYPE_ACCELEROMETER -> {
                val x = event.values[0]
                val y = event.values[1]
                val z = event.values[2]
                val step = detector.onSample(x, y, z, event.timestamp)
                _reading.value = AccelReading(x, y, z, detector.smoothed)

                traceBuffer.addLast(detector.smoothed)
                if (traceBuffer.size > TRACE_SIZE) traceBuffer.removeFirst()
                _trace.value = traceBuffer.toList()

                if (step && countingSteps && stepSource == StepSource.ACCELEROMETER) {
                    scope.launch { repo.addSteps(1) }
                }
            }

            Sensor.TYPE_STEP_COUNTER -> if (countingSteps) {
                // Copy the value out now: the system reuses SensorEvent objects.
                val total = event.values[0].toLong()
                scope.launch { repo.onHardwareTotal(total) }
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit

    private companion object {
        const val TRACE_SIZE = 150
    }
}
