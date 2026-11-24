package io.github.uveshm003.unreel.core.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import io.github.uveshm003.unreel.core.model.SetupStep
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

class OnboardingRepository @Inject constructor(private val store: DataStore<Preferences>) {
    val step: Flow<SetupStep> = store.data.map { it.setupStep() }.distinctUntilChanged()

    suspend fun advance(from: SetupStep) {
        SetupStep.entries.getOrNull(from.ordinal + 1)?.let { move(from, it) }
    }

    suspend fun skipTips() = move(SetupStep.Service, SetupStep.Complete)

    suspend fun back(from: SetupStep) {
        if (from == SetupStep.Complete) return
        SetupStep.entries.getOrNull(from.ordinal - 1)?.let { move(from, it) }
    }

    private suspend fun move(from: SetupStep, to: SetupStep) {
        store.edit { preferences ->
            if (preferences.setupStep() == from) preferences[StepKey] = to.key
        }
    }

    private fun Preferences.setupStep(): SetupStep = SetupStep.fromKey(this[StepKey])

    private companion object {
        val StepKey = stringPreferencesKey("onboarding_step")
    }
}
