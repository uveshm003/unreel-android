package io.github.uveshm003.unreel.feature.apps

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.uveshm003.unreel.core.data.FeedPreviewRepository
import java.io.IOException
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
internal class PreviewViewModel @Inject constructor(private val preview: FeedPreviewRepository) : ViewModel() {
    val error = MutableStateFlow(false)
    val enabled = preview.enabled.catch {
        error.value = true
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)
    val connected = preview.connected
    fun setEnabled(value: Boolean) {
        viewModelScope.launch {
            try {
                preview.setEnabled(value)
                error.value = false
            } catch (_: IOException) {
                error.value = true
            }
        }
    }
}
