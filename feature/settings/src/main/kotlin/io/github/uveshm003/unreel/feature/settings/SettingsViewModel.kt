package io.github.uveshm003.unreel.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.uveshm003.unreel.core.data.Appearance
import io.github.uveshm003.unreel.core.data.AppearanceRepository
import io.github.uveshm003.unreel.core.data.ThemeMode
import java.io.IOException
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SettingsUiState(val appearance: Appearance = Appearance(), val storageError: Boolean = false)

@HiltViewModel
class SettingsViewModel @Inject constructor(private val repository: AppearanceRepository) : ViewModel() {
    private val state = MutableStateFlow(SettingsUiState())
    val uiState = state.asStateFlow()
    private var reader: Job? = null

    init {
        retry()
    }

    fun retry() {
        reader?.cancel()
        reader = viewModelScope.launch {
            repository.appearance.catch { error ->
                if (error !is IOException) throw error
                state.update { it.copy(storageError = true) }
            }.collect { state.value = SettingsUiState(it) }
        }
    }

    fun setTheme(theme: ThemeMode) = save { repository.setTheme(theme) }

    fun setDynamicColor(enabled: Boolean) = save { repository.setDynamicColor(enabled) }

    private fun save(action: suspend () -> Unit) {
        viewModelScope.launch {
            try {
                action()
            } catch (_: IOException) {
                state.update { it.copy(storageError = true) }
            }
        }
    }
}
