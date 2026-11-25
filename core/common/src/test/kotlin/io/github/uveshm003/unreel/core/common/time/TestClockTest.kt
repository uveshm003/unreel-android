package io.github.uveshm003.unreel.core.common.time

import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.time.ZoneOffset
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class TestClockTest {
    private val start = Instant.parse("2026-03-01T10:00:00Z")

    @Test
    fun defaultsToAMondayMorningInUtc() {
        val clock = TestClock()
        assertEquals(TestClock.DEFAULT_START, clock.instant())
        assertEquals(ZoneOffset.UTC, clock.zone)
        assertEquals(java.time.DayOfWeek.MONDAY, clock.instant().atZone(clock.zone).dayOfWeek)
    }

    @Test
    fun advanceBy_movesTimeForward() {
        val clock = TestClock(start)
        clock.advanceBy(Duration.ofMinutes(90))
        assertEquals(start.plusSeconds(5_400), clock.instant())
        assertEquals(start.plusSeconds(5_400).toEpochMilli(), clock.millis())
    }

    @Test
    fun advanceBy_rejectsNegativeDurations() {
        assertFailsWith<IllegalArgumentException> { TestClock(start).advanceBy(Duration.ofSeconds(-1)) }
    }

    @Test
    fun setInstant_canMoveTimeBackwards() {
        val clock = TestClock(start)
        clock.setInstant(start.minusSeconds(3_600))
        assertEquals(start.minusSeconds(3_600), clock.instant())
    }

    @Test
    fun setZone_changesZoneButNotInstant() {
        val clock = TestClock(start)
        val kolkata = ZoneId.of("Asia/Kolkata")
        clock.setZone(kolkata)
        assertEquals(kolkata, clock.zone)
        assertEquals(start, clock.instant())
    }

    @Test
    fun withZone_sharesTimeWithTheParentClock() {
        val clock = TestClock(start)
        val tokyo = ZoneId.of("Asia/Tokyo")
        val view = clock.withZone(tokyo)

        clock.advanceBy(Duration.ofHours(1))

        assertEquals(tokyo, view.zone)
        assertEquals(clock.instant(), view.instant())
        assertEquals(ZoneOffset.UTC, view.withZone(ZoneOffset.UTC).zone)
        assertEquals(clock.instant(), view.withZone(ZoneOffset.UTC).instant())
    }

    @Test
    fun advancingAcrossDst_skipsTheMissingLocalHour() {
        val berlin = ZoneId.of("Europe/Berlin")
        // 2026-03-29: clocks in Berlin jump from 02:00 to 03:00 local (01:00 UTC).
        val clock = TestClock(Instant.parse("2026-03-29T00:59:00Z"), berlin)
        assertEquals(java.time.LocalTime.of(1, 59), clock.instant().atZone(clock.zone).toLocalTime())

        clock.advanceBy(Duration.ofMinutes(1))

        assertEquals(java.time.LocalTime.of(3, 0), clock.instant().atZone(clock.zone).toLocalTime())
    }
}
