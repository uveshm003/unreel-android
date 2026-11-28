package io.github.uveshm003.unreel.core.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import java.time.Clock
import java.time.Duration
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZonedDateTime
import javax.inject.Inject
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map

private val DEFAULT_START = LocalTime.of(22, 0)
private val DEFAULT_END = LocalTime.of(7, 0)

data class FocusWindow(
    val enabled: Boolean = false,
    val start: LocalTime = DEFAULT_START,
    val end: LocalTime = DEFAULT_END,
) {
    fun contains(time: LocalTime): Boolean = enabled && when {
        start < end -> time >= start && time < end
        start > end -> time >= start || time < end
        else -> true
    }
}

data class FeedDay(val date: LocalDate, val exits: Int, val viewingMillis: Long)
data class ProtectionSummary(val window: FocusWindow = FocusWindow(), val days: List<FeedDay> = emptyList())

class ProtectionActivity @Inject constructor(private val store: DataStore<Preferences>, private val clock: Clock) {
    private val enabledKey = booleanPreferencesKey("focus_enabled")
    private val startKey = intPreferencesKey("focus_start_seconds")
    private val endKey = intPreferencesKey("focus_end_seconds")
    private val dates = flow {
        while (true) {
            val now = ZonedDateTime.now(clock)
            emit(now.toLocalDate())
            // Also refresh after a system clock or zone change while the screen is open.
            delay(minOf(Duration.between(now, now.toLocalDate().plusDays(1).atStartOfDay(now.zone)).toMillis(), MINUTE))
        }
    }.distinctUntilChanged()
    val summary = combine(store.data, dates) { prefs, today -> read(prefs, today) }.distinctUntilChanged()
    val window = store.data.map(::readWindow).distinctUntilChanged()

    suspend fun setEnabled(value: Boolean) {
        store.edit { it[enabledKey] = value }
    }
    suspend fun setTime(start: Boolean, time: LocalTime) {
        store.edit { it[if (start) startKey else endKey] = time.toSecondOfDay() }
    }

    suspend fun snapshot(): ProtectionSummary = read(store.data.first(), LocalDate.now(clock))
    fun now(): LocalTime = LocalTime.now(clock)
    fun today(): LocalDate = LocalDate.now(clock)

    suspend fun record(date: LocalDate, millis: Long = 0L, exits: Int = 0) {
        require(millis >= 0 && exits >= 0)
        store.edit { prefs ->
            val cutoff = today().minusDays(WEEK - 1L)
            for (key in prefs.asMap().keys) {
                val day = key.name.removePrefix(PREFIX).substringBefore('_').toLongOrNull()
                if (key.name.startsWith(PREFIX) && day != null && day < cutoff.toEpochDay()) prefs.remove(key)
            }
            if (date >= cutoff && (millis > 0 || exits > 0)) {
                prefs[duration(date)] = (prefs[duration(date)] ?: 0L) + millis
                prefs[count(date)] = (prefs[count(date)] ?: 0) + exits
            }
        }
    }

    private fun readWindow(prefs: Preferences): FocusWindow {
        val defaults = FocusWindow()
        return FocusWindow(
            prefs[enabledKey] ?: false,
            prefs[startKey]?.let { LocalTime.ofSecondOfDay(it.toLong()) } ?: defaults.start,
            prefs[endKey]?.let { LocalTime.ofSecondOfDay(it.toLong()) } ?: defaults.end,
        )
    }

    private fun read(prefs: Preferences, today: LocalDate) = ProtectionSummary(
        readWindow(prefs),
        (0 until WEEK).map {
            val day = today.minusDays(it.toLong())
            FeedDay(day, prefs[count(day)] ?: 0, prefs[duration(day)] ?: 0L)
        },
    )

    private fun duration(day: LocalDate) = longPreferencesKey("${PREFIX}${day.toEpochDay()}_ms")
    private fun count(day: LocalDate) = intPreferencesKey("${PREFIX}${day.toEpochDay()}_exits")

    private companion object {
        const val WEEK = 7
        const val MINUTE = 60_000L
        const val PREFIX = "shorts_day_"
    }
}
