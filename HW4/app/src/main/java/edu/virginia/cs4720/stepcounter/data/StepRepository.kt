package edu.virginia.cs4720.stepcounter.data

import android.content.SharedPreferences
import androidx.core.content.edit
import edu.virginia.cs4720.stepcounter.sensors.StepDetector
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

/** How long daily totals are kept before being deleted automatically. */
const val RETENTION_DAYS = 90L

/**
 * Single place that reads/writes step data. Room holds the daily history;
 * SharedPreferences holds small settings and the hardware-counter baseline.
 */
class StepRepository(
    private val dao: StepDao,
    private val prefs: SharedPreferences,
) {
    val history: Flow<List<DailySteps>> = dao.observeAll()

    /** Make sure today has a row (so it shows in history) and drop expired days. */
    suspend fun startDay(today: LocalDate = LocalDate.now()) {
        dao.ensureDay(today.toString())
        dao.deleteBefore(today.minusDays(RETENTION_DAYS).toString())
    }

    suspend fun addSteps(delta: Long, today: LocalDate = LocalDate.now()) {
        if (delta > 0) dao.addSteps(today.toString(), delta)
    }

    /**
     * Called with each reading of Sensor.TYPE_STEP_COUNTER, which reports the total
     * number of steps since the phone last booted. We remember the previous reading
     * and add the difference to today's row.
     */
    suspend fun onHardwareTotal(total: Long, today: LocalDate = LocalDate.now()) {
        val delta = hardwareDelta(
            lastTotal = prefs.getLong(KEY_LAST_TOTAL, -1L),
            lastDate = prefs.getString(KEY_LAST_DATE, null),
            total = total,
            today = today.toString(),
        )
        prefs.edit {
            putLong(KEY_LAST_TOTAL, total)
            putString(KEY_LAST_DATE, today.toString())
        }
        addSteps(delta, today)
    }

    /**
     * Forget the hardware baseline. Called whenever the permission is not granted, so
     * that steps taken while the user had refused are never counted later.
     */
    fun clearHardwareBaseline() = prefs.edit {
        remove(KEY_LAST_TOTAL)
        remove(KEY_LAST_DATE)
    }

    suspend fun deleteAllHistory() {
        dao.deleteAll()
        clearHardwareBaseline()
    }

    var threshold: Float
        get() = prefs.getFloat(KEY_THRESHOLD, StepDetector.DEFAULT_THRESHOLD)
        set(value) = prefs.edit { putFloat(KEY_THRESHOLD, value) }

    var permissionAsked: Boolean
        get() = prefs.getBoolean(KEY_ASKED, false)
        set(value) = prefs.edit { putBoolean(KEY_ASKED, value) }

    private companion object {
        const val KEY_LAST_TOTAL = "last_hw_total"
        const val KEY_LAST_DATE = "last_hw_date"
        const val KEY_THRESHOLD = "threshold"
        const val KEY_ASKED = "permission_asked"
    }
}

/**
 * How many new steps a hardware counter reading represents.
 *
 * - No previous reading, or the previous reading was on an earlier day: this reading
 *   becomes today's starting value, so 0 new steps. We can't tell which of the steps
 *   since the last reading happened today, so we don't guess.
 * - The total went down: the phone rebooted and the counter restarted from 0, so
 *   everything it reports now is new.
 * - Otherwise: the difference.
 */
fun hardwareDelta(lastTotal: Long, lastDate: String?, total: Long, today: String): Long = when {
    lastTotal < 0 || lastDate != today -> 0
    total < lastTotal -> total
    else -> total - lastTotal
}
