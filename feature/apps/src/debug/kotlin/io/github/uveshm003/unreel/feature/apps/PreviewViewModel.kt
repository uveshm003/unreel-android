package io.github.uveshm003.unreel.feature.apps

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.uveshm003.unreel.core.common.dispatchers.Dispatcher
import io.github.uveshm003.unreel.core.common.dispatchers.UnreelDispatchers
import io.github.uveshm003.unreel.core.data.FeedPreviewRepository
import io.github.uveshm003.unreel.core.data.PreviewCompatibility
import io.github.uveshm003.unreel.core.data.PreviewDevice
import java.io.IOException
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

internal data class PreviewStatus(val compatibility: PreviewCompatibility, val serviceEnabled: Boolean?)

@HiltViewModel
internal class PreviewViewModel @Inject constructor(
    private val preview: FeedPreviewRepository,
    private val device: PreviewDevice,
    @Dispatcher(UnreelDispatchers.IO) private val io: CoroutineDispatcher,
) : ViewModel() {
    val error = MutableStateFlow(false)
    val enabled = preview.enabled.catch {
        error.value = true
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)
    val connected = preview.connected
    val status = MutableStateFlow<PreviewStatus?>(null)
    private var refreshJob: Job? = null

    fun refresh() {
        refreshJob?.cancel()
        status.value = null
        refreshJob = viewModelScope.launch {
            status.value = withContext(io) { PreviewStatus(device.compatibility(), device.serviceEnabled()) }
        }
    }

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
