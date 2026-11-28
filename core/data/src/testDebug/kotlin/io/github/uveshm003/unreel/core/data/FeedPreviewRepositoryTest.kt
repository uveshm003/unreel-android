package io.github.uveshm003.unreel.core.data

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import java.io.File
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class FeedPreviewRepositoryTest {
    @get:Rule val folder = TemporaryFolder()

    @Test
    fun activationRequiresCurrentConsentAndRevocationSurvivesReopening() = runTest {
        val store = PreferenceDataStoreFactory.create(scope = backgroundScope) {
            File(folder.root, "setup.preferences_pb")
        }
        val clock = Clock.fixed(Instant.EPOCH, ZoneOffset.UTC)
        val repo = FeedPreviewRepository(store, clock)
        repo.connectionChanged(true)
        assertFalse(repo.enabled.first())
        repo.setEnabled(true)
        assertTrue(FeedPreviewRepository(store, clock).enabled.first())
        store.edit { it[intPreferencesKey("feed_preview_consent_version")] = 0 }
        assertFalse(repo.enabled.first())
        repo.setEnabled(true)
        repo.setEnabled(false)
        assertFalse(FeedPreviewRepository(store, clock).enabled.first())
    }
}
