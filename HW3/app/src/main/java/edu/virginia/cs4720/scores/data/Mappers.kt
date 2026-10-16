package edu.virginia.cs4720.scores.data

import edu.virginia.cs4720.scores.data.local.GameEntity
import edu.virginia.cs4720.scores.data.remote.GameDto

fun GameDto.toEntity(division: String, season: Int, week: Int) = GameEntity(
    gameId = gameID,
    division = division,
    season = season,
    week = week,
    state = gameState,
    period = currentPeriod,
    clock = contestClock,
    finalMessage = finalMessage,
    startEpoch = startTimeEpoch.toLongOrNull() ?: 0L,
    homeName = home.names.short,
    awayName = away.names.short,
    homeScore = home.score.toIntOrNull(),
    awayScore = away.score.toIntOrNull(),
    homeWinner = home.winner,
    awayWinner = away.winner,
)