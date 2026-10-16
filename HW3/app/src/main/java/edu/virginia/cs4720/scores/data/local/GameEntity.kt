package edu.virginia.cs4720.scores.data.local

import androidx.room.Entity
import androidx.room.Index

@Entity(
    tableName = "games",
    primaryKeys = ["gameId", "division"],
    indices = [Index("division", "season", "week")]
)
data class GameEntity(
    val gameId: String,
    val division: String,
    val season: Int,
    val week: Int,
    val state: String,
    val period: String,
    val clock: String,
    val finalMessage: String,
    val startEpoch: Long,
    val homeName: String,
    val awayName: String,
    val homeScore: Int?,
    val awayScore: Int?,
    val homeWinner: Boolean,
    val awayWinner: Boolean,
)