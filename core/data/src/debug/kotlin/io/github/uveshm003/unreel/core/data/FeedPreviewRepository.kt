package io.github.uveshm003.unreel.core.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import java.time.Clock
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

@Singleton
class FeedPreviewRepository @Inject constructor(private val store: DataStore<Preferences>, private val clock: Clock) {
    private val connection = MutableStateFlow(false)
    val connected = connection.asStateFlow()
    val enabled = store.data.map { it[version] == CURRENT_DISCLOSURE && it[acceptedAt] != null }.distinctUntilChanged()

    @Volatile private var passUntil = 0L
    fun allowFiveMinutes() {
        passUntil = android.os.SystemClock.elapsedRealtime() + FIVE_MINUTES
    }
    fun remainingPass(): Long = (passUntil - android.os.SystemClock.elapsedRealtime()).coerceAtLeast(0L)

    fun connectionChanged(connected: Boolean) {
        connection.value = connected
        if (!connected) passUntil = 0L
    }

    suspend fun setEnabled(enabled: Boolean) {
        if (!enabled) passUntil = 0L
        store.edit {
            if (enabled) {
                it[version] = CURRENT_DISCLOSURE
                it[acceptedAt] = clock.millis()
            } else {
                it.remove(version)
                it.remove(acceptedAt)
            }
        }
    }

    private companion object {
        const val CURRENT_DISCLOSURE = 2
        const val FIVE_MINUTES = 300_000L
        val version = intPreferencesKey("feed_preview_consent_version")
        val acceptedAt = longPreferencesKey("feed_preview_accepted_at")
    }
}
