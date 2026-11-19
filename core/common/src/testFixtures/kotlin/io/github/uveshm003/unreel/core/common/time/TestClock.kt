package io.github.uveshm003.unreel.core.common.time

import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.time.ZoneOffset
import java.util.concurrent.atomic.AtomicReference

/**
 * A settable, zone-aware [Clock] for tests. [advanceBy] models time passing; [setInstant] can also move time
 * backwards (`TIME_SET`), and [setZone] models `TIMEZONE_CHANGED`. Clocks from [withZone] share this clock's time.
 */
class TestClock(start: Instant = DEFAULT_START, zone: ZoneId = ZoneOffset.UTC) : Clock() {
    private val now = AtomicReference(start)

    @Volatile
    private var currentZone: ZoneId = zone

    override fun instant(): Instant = now.get()

    override fun getZone(): ZoneId = currentZone

    override fun withZone(zone: ZoneId): Clock = ZonedView(zone)

    fun advanceBy(duration: Duration) {
        require(!duration.isNegative) { "Time only moves forward with advanceBy; use setInstant to move it back" }
        now.updateAndGet { it + duration }
    }

    fun setInstant(instant: Instant) {
        now.set(instant)
    }

    fun setZone(zone: ZoneId) {
        currentZone = zone
    }

    private inner class ZonedView(private val viewZone: ZoneId) : Clock() {
        override fun instant(): Instant = this@TestClock.instant()

        override fun getZone(): ZoneId = viewZone

        override fun withZone(zone: ZoneId): Clock = this@TestClock.withZone(zone)
    }

    companion object {
        /** A Monday morning, so weekday logic has a predictable starting point. */
        val DEFAULT_START: Instant = Instant.parse("2026-01-05T09:00:00Z")
    }
}
