package io.github.uveshm003.unreel.core.data

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import java.io.File
import java.time.Clock
import java.time.Instant
import java.time.LocalTime
import java.time.ZoneOffset
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class ProtectionActivityTest {
    @get:Rule val folder = TemporaryFolder()

    @Test
    fun dailyWindowUsesInclusiveStartAndExclusiveEndAcrossMidnight() {
        val overnight = FocusWindow(enabled = true)
        assertTrue(overnight.contains(overnight.start))
        assertTrue(overnight.contains(LocalTime.MIDNIGHT))
        assertFalse(overnight.contains(overnight.end))
        assertFalse(overnight.contains(LocalTime.NOON))
        assertFalse(overnight.copy(enabled = false).contains(LocalTime.MIDNIGHT))
        val daytime = overnight.copy(start = overnight.end, end = overnight.start)
        assertTrue(daytime.contains(LocalTime.NOON))
        assertFalse(daytime.contains(LocalTime.MIDNIGHT))
        assertTrue(daytime.copy(end = daytime.start).contains(LocalTime.MIDNIGHT))
    }

    @Test
    fun preferencesAndDailyTotalsSurviveReopeningAndUseLocalCalendarDays() = runTest {
        val store = PreferenceDataStoreFactory.create(scope = backgroundScope) {
            File(folder.root, "activity.preferences_pb")
        }
        val clock = Clock.fixed(Instant.parse("2026-11-27T23:30:00Z"), ZoneOffset.ofHours(2))
        val activity = ProtectionActivity(store, clock)
        val today = activity.today()
        activity.setEnabled(true)
        activity.setTime(true, LocalTime.NOON)
        val priorExits = 2
        activity.record(today, FIRST_VISIT)
        activity.record(today, SECOND_VISIT, 1)
        activity.record(today.minusDays(1), exits = priorExits)
        activity.record(today.minusWeeks(1), exits = 42)
        val snapshot = ProtectionActivity(store, clock).snapshot()
        assertEquals(FocusWindow(true, LocalTime.NOON), snapshot.window)
        assertEquals(DAYS_IN_WEEK, snapshot.days.size)
        assertEquals(FeedDay(today, 1, FIRST_VISIT + SECOND_VISIT), snapshot.days.first())
        assertEquals(2, snapshot.days[1].exits)
        assertEquals(1 + priorExits, snapshot.days.sumOf { it.exits })
    }
}

private const val FIRST_VISIT = 5_000L

private const val SECOND_VISIT = 7_000L

private const val DAYS_IN_WEEK = 7
