package edu.virginia.cs4720.scores.data.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface GameDao {
    @Query(
        """SELECT * FROM games
           WHERE division = :division AND season = :season AND week = :week
           ORDER BY startEpoch, gameId"""
    )
    fun observe(division: String, season: Int, week: Int): Flow<List<GameEntity>>

    @Upsert
    suspend fun upsertAll(games: List<GameEntity>)
}