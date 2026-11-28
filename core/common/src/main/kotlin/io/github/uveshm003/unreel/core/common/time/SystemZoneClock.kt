package io.github.uveshm003.unreel.core.common.time

import java.time.Clock
import java.time.Instant
import java.time.ZoneId

/**
 * Wall-clock time in the device's *current* time zone.
 *
 * `Clock.systemDefaultZone()` captures the zone once, so an injected singleton would keep the old zone after
 * `TIMEZONE_CHANGED`, and budgets would reset at the wrong midnight. This clock re-reads [ZoneId.systemDefault]
 * on every call (Android updates the default zone when the broadcast arrives).
 */
object SystemZoneClock : Clock() {
    override fun getZone(): ZoneId = ZoneId.systemDefault()

    override fun withZone(zone: ZoneId): Clock = system(zone)

    override fun instant(): Instant = Instant.now()

    override fun millis(): Long = System.currentTimeMillis()
}
