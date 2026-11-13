package edu.virginia.cs4720.stepcounter

import edu.virginia.cs4720.stepcounter.data.hardwareDelta
import edu.virginia.cs4720.stepcounter.sensors.StepDetector
import org.junit.Assert.assertEquals
import org.junit.Test
import kotlin.math.PI
import kotlin.math.sin

class StepDetectorTest {

    private val sampleNs = 20_000_000L // 50 Hz, like SENSOR_DELAY_GAME

    /** Simulates walking: gravity on z plus a bounce of [amplitude] at [stepsPerSecond]. */
    private fun countSteps(seconds: Int, stepsPerSecond: Double, amplitude: Double): Int {
        val detector = StepDetector()
        var steps = 0
        val samples = seconds * 50
        for (i in 0 until samples) {
            val t = i * 0.02
            val z = 9.81 + amplitude * sin(2 * PI * stepsPerSecond * t)
            if (detector.onSample(0f, 0f, z.toFloat(), i * sampleNs)) steps++
        }
        return steps
    }

    @Test
    fun walkingAtTwoStepsPerSecondCountsAboutTwentyStepsInTenSeconds() {
        val steps = countSteps(seconds = 10, stepsPerSecond = 2.0, amplitude = 3.0)
        assertEquals(20.0, steps.toDouble(), 1.0)
    }

    @Test
    fun phoneLyingStillCountsNothing() {
        assertEquals(0, countSteps(seconds = 10, stepsPerSecond = 2.0, amplitude = 0.0))
    }

    @Test
    fun smallJiggleBelowThresholdCountsNothing() {
        assertEquals(0, countSteps(seconds = 10, stepsPerSecond = 2.0, amplitude = 0.5))
    }

    @Test
    fun peaksCloserThanMinimumIntervalAreIgnored() {
        // 5 "steps" per second is faster than the 0.3 s limit allows.
        val steps = countSteps(seconds = 10, stepsPerSecond = 5.0, amplitude = 3.0)
        assert(steps <= 34) { "expected at most ~33 steps, got $steps" }
    }
}

class HardwareDeltaTest {
    @Test fun firstReadingIsBaseline() = assertEquals(0L, hardwareDelta(-1, null, 5000, "2026-11-13"))
    @Test fun sameDayAddsDifference() = assertEquals(120L, hardwareDelta(5000, "2026-11-13", 5120, "2026-11-13"))
    @Test fun newDayResetsBaseline() = assertEquals(0L, hardwareDelta(5000, "2026-11-12", 5300, "2026-11-13"))
    @Test fun rebootCountsStepsSinceBoot() = assertEquals(40L, hardwareDelta(5000, "2026-11-13", 40, "2026-11-13"))
}
