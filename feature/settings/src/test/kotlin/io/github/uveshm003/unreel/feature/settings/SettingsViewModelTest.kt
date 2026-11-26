package io.github.uveshm003.unreel.feature.settings

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.mutablePreferencesOf
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.lifecycle.viewModelScope
import io.github.uveshm003.unreel.core.data.Appearance
import io.github.uveshm003.unreel.core.data.AppearanceRepository
import io.github.uveshm003.unreel.core.data.ThemeMode
import java.io.IOException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SettingsViewModelTest {
    @Test
    fun selectionsSurviveANewViewModelAndUnknownThemeFollowsSystem() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val store = TestPreferences()
        store.preferences.value = mutablePreferencesOf(stringPreferencesKey("appearance_theme") to "unknown")
        val first = SettingsViewModel(AppearanceRepository(store))
        try {
            runCurrent()
            assertEquals(Appearance(), first.uiState.value.appearance)
            assertTrue(ThemeMode.System.isDark(true))
            assertFalse(ThemeMode.System.isDark(false))
            assertFalse(ThemeMode.Light.isDark(true))
            assertTrue(ThemeMode.Dark.isDark(false))
            first.setTheme(ThemeMode.Dark)
            first.setDynamicColor(false)
            runCurrent()
            val reopened = SettingsViewModel(AppearanceRepository(store))
            try {
                runCurrent()
                assertEquals(Appearance(ThemeMode.Dark, false), reopened.uiState.value.appearance)
            } finally {
                reopened.viewModelScope.cancel()
            }
        } finally {
            first.viewModelScope.cancel()
            Dispatchers.resetMain()
        }
    }

    @Test
    fun reportsStorageFailuresAndCanReloadTheActualPreferences() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val store = TestPreferences().apply { fail = true }
        val vm = SettingsViewModel(AppearanceRepository(store))
        try {
            runCurrent()
            assertTrue(vm.uiState.value.storageError)
            store.fail = false
            vm.retry()
            vm.setTheme(ThemeMode.Light)
            runCurrent()
            assertEquals(SettingsUiState(Appearance(ThemeMode.Light)), vm.uiState.value)
            store.fail = true
            vm.setDynamicColor(false)
            runCurrent()
            assertTrue(vm.uiState.value.storageError)
            assertTrue(vm.uiState.value.appearance.dynamicColor)
        } finally {
            vm.viewModelScope.cancel()
            Dispatchers.resetMain()
        }
    }

    private class TestPreferences : DataStore<Preferences> {
        val preferences = MutableStateFlow(emptyPreferences())
        var fail = false
        override val data = flow {
            if (fail) throw IOException()
            emitAll(preferences)
        }
        override suspend fun updateData(transform: suspend (Preferences) -> Preferences): Preferences {
            if (fail) throw IOException()
            return transform(preferences.value).also { preferences.value = it }
        }
    }
}
