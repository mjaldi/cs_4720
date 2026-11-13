package edu.virginia.cs4720.stepcounter.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * One row per day. We deliberately store only a daily total: no timestamps of
 * individual steps, no raw sensor readings, no location.
 *
 * [date] is an ISO-8601 string (yyyy-MM-dd) so it sorts correctly as text.
 */
@Entity(tableName = "daily_steps")
data class DailySteps(
    @PrimaryKey val date: String,
    val steps: Long,
)
