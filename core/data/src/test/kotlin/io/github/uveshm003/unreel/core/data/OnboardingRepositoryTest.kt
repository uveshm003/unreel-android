package io.github.uveshm003.unreel.core.data

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import io.github.uveshm003.unreel.core.model.SetupStep
import java.io.File
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class OnboardingRepositoryTest {
    @get:Rule val folder = TemporaryFolder()

    @Test
    fun aNewStoreResumesAtTheLastCommittedStep() = runTest {
        val file = File(folder.root, "setup.preferences_pb")
        val job = Job()
        val scope = CoroutineScope(Dispatchers.IO + job)
        val first = OnboardingRepository(PreferenceDataStoreFactory.create(scope = scope) { file })
        try {
            assertEquals(SetupStep.Welcome, first.step.first())
            first.advance(SetupStep.Welcome)
        } finally {
            job.cancelAndJoin()
        }
        val reopened = OnboardingRepository(PreferenceDataStoreFactory.create(scope = backgroundScope) { file })
        assertEquals(SetupStep.Disclosure, reopened.step.first())
    }

    @Test
    fun duplicateActionsCannotSkipStepsAndCompletionIsTerminal() = runTest {
        val file = File(folder.root, "setup.preferences_pb")
        val store = PreferenceDataStoreFactory.create(scope = backgroundScope) { file }
        val repo = OnboardingRepository(store)
        store.edit { it[stringPreferencesKey("onboarding_step")] = "unknown" }
        assertEquals(SetupStep.Welcome, repo.step.first())
        repo.skipTips()
        listOf(launch { repo.advance(SetupStep.Welcome) }, launch { repo.advance(SetupStep.Welcome) }).joinAll()
        assertEquals(SetupStep.Disclosure, repo.step.first())
        repo.back(SetupStep.Disclosure)
        assertEquals(SetupStep.Welcome, repo.step.first())
        repo.advance(SetupStep.Welcome)
        repo.advance(SetupStep.Disclosure)
        repo.skipTips()
        repo.back(SetupStep.Complete)
        assertEquals(SetupStep.Complete, repo.step.first())
    }
}
