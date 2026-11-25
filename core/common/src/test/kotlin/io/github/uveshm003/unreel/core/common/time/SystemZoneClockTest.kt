package io.github.uveshm003.unreel.core.common.time

import java.time.Instant
import java.time.ZoneId
import java.util.TimeZone
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame
import kotlin.test.assertTrue

class SystemZoneClockTest {
    private val originalZone: TimeZone = TimeZone.getDefault()

    @AfterTest
    fun restoreZone() = TimeZone.setDefault(originalZone)

    @Test
    fun zone_followsTheCurrentSystemDefault() {
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Kolkata"))
        assertEquals(ZoneId.of("Asia/Kolkata"), SystemZoneClock.zone)

        // Simulates TIMEZONE_CHANGED: the same clock instance must pick up the new zone.
        TimeZone.setDefault(TimeZone.getTimeZone("Europe/Berlin"))
        assertEquals(ZoneId.of("Europe/Berlin"), SystemZoneClock.zone)
    }

    @Test
    fun instantAndMillis_areCurrentTime() {
        val before = Instant.now()
        val instant = SystemZoneClock.instant()
        val millis = SystemZoneClock.millis()
        val after = Instant.now()

        assertTrue(!instant.isBefore(before) && !instant.isAfter(after))
        assertTrue(millis in before.toEpochMilli()..after.toEpochMilli())
    }

    @Test
    fun withZone_returnsAClockInThatZone() {
        val tokyo = ZoneId.of("Asia/Tokyo")
        assertEquals(tokyo, SystemZoneClock.withZone(tokyo).zone)
    }

    @Test
    fun timeModule_providesTheSystemZoneClock() {
        assertSame(SystemZoneClock, TimeModule.providesClock())
    }
}
