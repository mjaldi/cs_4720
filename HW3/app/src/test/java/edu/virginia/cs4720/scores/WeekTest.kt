package edu.virginia.cs4720.scores

import edu.virginia.cs4720.scores.data.defaultWeek
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Test

class WeekTest {
    @Test fun week7() = assertEquals(7, defaultWeek(LocalDate.of(2026, 10, 15)))
    @Test fun week7StartsTuesday() = assertEquals(7, defaultWeek(LocalDate.of(2026, 10, 13)))
    @Test fun mondayIsStillWeek6() = assertEquals(6, defaultWeek(LocalDate.of(2026, 10, 12)))
    @Test fun clampsLow() = assertEquals(1, defaultWeek(LocalDate.of(2026, 8, 1)))
    @Test fun clampsHigh() = assertEquals(14, defaultWeek(LocalDate.of(2026, 12, 25)))
}