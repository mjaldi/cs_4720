package edu.virginia.cs4720.scores.data

import edu.virginia.cs4720.scores.data.local.GameEntity
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

enum class GameStatus { UPCOMING, LIVE, FINAL }

fun GameEntity.status(): GameStatus = when (state.lowercase()) {
    "pre" -> GameStatus.UPCOMING
    "final" -> GameStatus.FINAL
    else -> GameStatus.LIVE
}

fun GameEntity.isOvertime(): Boolean =
    status() == GameStatus.FINAL &&
            Regex("""\bOT\b|\dOT\b|OVERTIME""", RegexOption.IGNORE_CASE)
                .containsMatchIn("$finalMessage $period")

fun GameEntity.statusText(): String = when (status()) {
    GameStatus.UPCOMING ->
        Instant.ofEpochSecond(startEpoch)
            .atZone(ZoneId.systemDefault())
            .format(DateTimeFormatter.ofPattern("EEE, MMM d · h:mm a"))
    GameStatus.LIVE -> listOf(period, clock).filter { it.isNotBlank() }.joinToString(" · ")
    GameStatus.FINAL -> if (isOvertime()) "Final/OT" else "Final"
}