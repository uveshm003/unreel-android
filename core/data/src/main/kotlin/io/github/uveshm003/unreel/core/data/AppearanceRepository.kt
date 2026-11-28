package io.github.uveshm003.unreel.core.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import javax.inject.Inject
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

enum class ThemeMode(val key: String) {
    System("system"),
    Light("light"),
    Dark("dark"),
    ;

    fun isDark(systemDark: Boolean): Boolean = this == Dark || (this == System && systemDark)
}

data class Appearance(val theme: ThemeMode = ThemeMode.System, val dynamicColor: Boolean = true)

class AppearanceRepository @Inject constructor(private val store: DataStore<Preferences>) {
    private val themeKey = stringPreferencesKey("appearance_theme")
    private val dynamicKey = booleanPreferencesKey("appearance_dynamic_color")

    val appearance = store.data.map { prefs ->
        Appearance(ThemeMode.entries.find { it.key == prefs[themeKey] } ?: ThemeMode.System, prefs[dynamicKey] ?: true)
    }.distinctUntilChanged()

    suspend fun setTheme(theme: ThemeMode) {
        store.edit { it[themeKey] = theme.key }
    }

    suspend fun setDynamicColor(enabled: Boolean) {
        store.edit { it[dynamicKey] = enabled }
    }
}
