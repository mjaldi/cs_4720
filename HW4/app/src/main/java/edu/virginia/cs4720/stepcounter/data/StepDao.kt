package edu.virginia.cs4720.stepcounter.data

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
abstract class StepDao {

    /** All days, newest first. */
    @Query("SELECT * FROM daily_steps ORDER BY date DESC")
    abstract fun observeAll(): Flow<List<DailySteps>>

    @Query("INSERT OR IGNORE INTO daily_steps(date, steps) VALUES (:date, 0)")
    abstract suspend fun ensureDay(date: String)

    @Query("UPDATE daily_steps SET steps = steps + :delta WHERE date = :date")
    protected abstract suspend fun increment(date: String, delta: Long)

    /** Adds [delta] steps to [date], creating the row if needed. */
    @Transaction
    open suspend fun addSteps(date: String, delta: Long) {
        ensureDay(date)
        increment(date, delta)
    }

    @Query("DELETE FROM daily_steps WHERE date < :cutoff")
    abstract suspend fun deleteBefore(cutoff: String)

    @Query("DELETE FROM daily_steps")
    abstract suspend fun deleteAll()
}
