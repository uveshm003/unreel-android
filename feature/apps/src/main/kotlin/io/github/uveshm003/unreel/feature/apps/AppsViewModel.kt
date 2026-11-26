package io.github.uveshm003.unreel.feature.apps

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import io.github.uveshm003.unreel.core.common.dispatchers.Dispatcher
import io.github.uveshm003.unreel.core.common.dispatchers.UnreelDispatchers
import io.github.uveshm003.unreel.core.common.result.Outcome
import io.github.uveshm003.unreel.core.data.ProtectionChangeError
import io.github.uveshm003.unreel.core.data.ProtectionSettingsRepository
import io.github.uveshm003.unreel.core.model.AppProtection
import io.github.uveshm003.unreel.core.model.ProtectedApp
import io.github.uveshm003.unreel.core.model.ProtectionLevel
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

internal data class AppsUiState(
    val policies: Map<String, AppProtection> = emptyMap(),
    val installed: Set<ProtectedApp>? = null,
    val readFailed: Boolean = false,
)

internal class InstalledApps @Inject constructor(
    @ApplicationContext private val context: Context,
    @Dispatcher(UnreelDispatchers.IO) private val io: CoroutineDispatcher,
) {
    suspend fun read(): Set<ProtectedApp> = withContext(io) {
        ProtectedApp.entries.filterTo(mutableSetOf()) { app ->
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    context.packageManager.getPackageInfo(app.packageName, PackageManager.PackageInfoFlags.of(0))
                } else {
                    context.packageManager.getPackageInfo(app.packageName, 0)
                }
                true
            } catch (_: PackageManager.NameNotFoundException) {
                false
            }
        }
    }
}

@HiltViewModel
internal class AppsViewModel @Inject constructor(
    private val repository: ProtectionSettingsRepository,
    private val installedApps: InstalledApps,
) : ViewModel() {
    private val state = MutableStateFlow(AppsUiState())
    val uiState = state.asStateFlow()
    private val messages = Channel<Int>(Channel.BUFFERED)
    val errors = messages.receiveAsFlow()
    private var reader: Job? = null
    private var packageLookup: Job? = null

    init {
        retry()
    }

    fun retry() {
        reader?.cancel()
        reader = viewModelScope.launch {
            repository.settings.catch { state.update { it.copy(readFailed = true) } }
                .collect { policies -> state.update { it.copy(policies = policies, readFailed = false) } }
        }
    }

    fun refreshInstalled() {
        packageLookup?.cancel()
        packageLookup = viewModelScope.launch {
            val installed = try {
                installedApps.read()
            } catch (_: SecurityException) {
                null
            }
            state.update { it.copy(installed = installed) }
        }
    }

    fun setLevel(app: ProtectedApp, level: ProtectionLevel) = save { repository.setLevel(app.packageName, level) }

    fun setBudget(app: ProtectedApp, minutes: Int) = save { repository.setBudget(app.packageName, minutes) }

    fun setAllowOne(app: ProtectedApp, enabled: Boolean) = save { repository.setAllowOne(app.packageName, enabled) }

    private fun save(write: suspend () -> Outcome<Unit, ProtectionChangeError>) {
        viewModelScope.launch {
            try {
                when (write().errorOrNull()) {
                    ProtectionChangeError.InvalidBudget -> messages.send(R.string.apps_invalid_budget)
                    ProtectionChangeError.UnknownPackage -> messages.send(R.string.apps_save_failed)
                    null -> Unit
                }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                messages.send(R.string.apps_save_failed)
            }
        }
    }
}
