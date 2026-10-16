package edu.virginia.cs4720.scores

import edu.virginia.cs4720.scores.data.GameStatus
import edu.virginia.cs4720.scores.data.isOvertime
import edu.virginia.cs4720.scores.data.local.GameEntity
import edu.virginia.cs4720.scores.data.status
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class StatusTest {
    private fun game(state: String, msg: String = "", period: String = "") = GameEntity(
        gameId = "1", division = "fbs", season = 2026, week = 7,
        state = state, period = period, clock = "0:00", finalMessage = msg,
        startEpoch = 0L, homeName = "H", awayName = "A",
        homeScore = null, awayScore = null, homeWinner = false, awayWinner = false,
    )

    @Test fun upcoming() = assertEquals(GameStatus.UPCOMING, game("pre").status())
    @Test fun final() = assertEquals(GameStatus.FINAL, game("final", "FINAL", "FINAL").status())
    @Test fun unknownStateShowsAsLive() = assertEquals(GameStatus.LIVE, game("live").status())
    @Test fun normalFinalIsNotOvertime() = assertFalse(game("final", "FINAL", "FINAL").isOvertime())
    @Test fun overtimeFinal() = assertTrue(game("final", "FINAL (OT)").isOvertime())
    @Test fun doubleOvertimeFinal() = assertTrue(game("final", "FINAL/2OT").isOvertime())
}