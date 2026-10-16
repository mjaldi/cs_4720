package edu.virginia.cs4720.scores.data.remote

import kotlinx.serialization.Serializable

@Serializable
data class ScoreboardResponse(val games: List<GameWrapper> = emptyList())

@Serializable
data class GameWrapper(val game: GameDto)

@Serializable
data class GameDto(
    val gameID: String,
    val gameState: String = "",
    val currentPeriod: String = "",
    val contestClock: String = "",
    val finalMessage: String = "",
    val startTimeEpoch: String = "",
    val home: TeamDto = TeamDto(),
    val away: TeamDto = TeamDto(),
)

@Serializable
data class TeamDto(
    val score: String = "",
    val winner: Boolean = false,
    val names: NamesDto = NamesDto(),
)

@Serializable
data class NamesDto(val short: String = "")