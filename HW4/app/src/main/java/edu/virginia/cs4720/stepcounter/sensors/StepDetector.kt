package edu.virginia.cs4720.stepcounter.sensors

import kotlin.math.sqrt

/**
 * Accelerometer step counter used when the device has no hardware step counter.
 *
 * 1. magnitude = sqrt(x^2 + y^2 + z^2), so phone orientation doesn't matter
 * 2. smooth it with a moving average over the last [window] samples
 * 3. a step is a local peak of the smoothed signal that is above [threshold]
 *    and at least [minIntervalNs] after the previous step.
 *
 * Pure Kotlin with no Android types, so it can be unit-tested.
 */
class StepDetector(
    var threshold: Float = DEFAULT_THRESHOLD,
    private val window: Int = 5,
    private val minIntervalNs: Long = 300_000_000L, // 0.3 s, i.e. at most ~3.3 steps/s
) {
    private val buffer = ArrayDeque<Float>()
    private var sum = 0f

    private var prev2 = Float.NaN
    private var prev1 = Float.NaN
    private var prev1Time = 0L
    private var lastStepTime: Long? = null

    /** Most recent smoothed magnitude, for display. */
    var smoothed = 0f
        private set

    /** Feed one accelerometer sample; returns true if it completed a step. */
    fun onSample(x: Float, y: Float, z: Float, timestampNs: Long): Boolean {
        val magnitude = sqrt(x * x + y * y + z * z)
        buffer.addLast(magnitude)
        sum += magnitude
        if (buffer.size > window) sum -= buffer.removeFirst()
        val current = sum / buffer.size
        smoothed = current

        // prev1 is a peak if it rose from prev2 and the signal has now stopped rising.
        val isPeak = !prev2.isNaN() && prev1 > prev2 && prev1 >= current
        val last = lastStepTime
        val farEnough = last == null || prev1Time - last >= minIntervalNs
        val step = isPeak && prev1 > threshold && farEnough
        if (step) lastStepTime = prev1Time

        prev2 = prev1
        prev1 = current
        prev1Time = timestampNs
        return step
    }

    fun reset() {
        buffer.clear()
        sum = 0f
        prev2 = Float.NaN
        prev1 = Float.NaN
        lastStepTime = null
    }

    companion object {
        /** m/s^2. Gravity alone is ~9.8, so this means ~1.2 m/s^2 of extra acceleration. */
        const val DEFAULT_THRESHOLD = 11.0f
        const val MIN_THRESHOLD = 10.0f
        const val MAX_THRESHOLD = 15.0f
    }
}
