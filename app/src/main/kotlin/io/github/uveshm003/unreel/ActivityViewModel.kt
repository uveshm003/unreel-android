package io.github.uveshm003.unreel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.uveshm003.unreel.core.data.ProtectionActivity
import java.io.IOException
import java.time.LocalTime
import javax.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
internal class ActivityViewModel @Inject constructor(
    private val activity: ProtectionActivity,
    private val exporter: DataExport,
) : ViewModel() {
    private val failures = Channel<Unit>(Channel.CONFLATED)
    val errors = failures.receiveAsFlow()
    val summary = activity.summary.catch { error ->
        if (error !is IOException) throw error
        failures.send(Unit)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    private val exportResults = Channel<Boolean>(Channel.BUFFERED)
    val exports = exportResults.receiveAsFlow()
    fun export(uri: android.net.Uri) {
        viewModelScope.launch {
            val success = try {
                exporter.write(uri)
                true
            } catch (
                _: IOException,
            ) {
                false
            } catch (_: SecurityException) {
                false
            }
            exportResults.send(success)
        }
    }

    fun enable(value: Boolean) = save { activity.setEnabled(value) }
    fun setTime(start: Boolean, time: LocalTime) = save { activity.setTime(start, time) }

    private fun save(write: suspend () -> Unit) {
        viewModelScope.launch {
            try {
                write()
            } catch (_: IOException) {
                failures.send(Unit)
            }
        }
    }
}
