package edu.virginia.cs4720.scores.data

import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit

fun defaultWeek(today: LocalDate = LocalDate.now(ZoneId.of("America/New_York"))): Int {
    val week7Start = LocalDate.of(2026, 10, 13)
    val days = ChronoUnit.DAYS.between(week7Start, today)
    return (7 + Math.floorDiv(days, 7L)).toInt().coerceIn(1, 14)
}