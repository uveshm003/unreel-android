package io.github.uveshm003.unreel.feature.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.uveshm003.unreel.core.data.OnboardingRepository
import io.github.uveshm003.unreel.core.model.SetupStep
import java.io.IOException
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal sealed interface SetupState {
    data object Loading : SetupState
    data object StorageError : SetupState
    data class Ready(val step: SetupStep) : SetupState
}

@HiltViewModel
class OnboardingViewModel @Inject constructor(private val repository: OnboardingRepository) : ViewModel() {
    private val retryCount = MutableStateFlow(0)
    private val writeFailed = MutableStateFlow(false)
    internal val uiState = combine(
        retryCount.flatMapLatest {
            repository.step.map<SetupStep, SetupState> { SetupState.Ready(it) }.catch { error ->
                if (error !is IOException) throw error
                emit(SetupState.StorageError)
            }
        },
        writeFailed,
    ) { state, failed -> if (failed) SetupState.StorageError else state }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SetupState.Loading)

    fun advance(step: SetupStep) = write { repository.advance(step) }

    fun skipTips() = write { repository.skipTips() }

    fun back(step: SetupStep) = write { repository.back(step) }

    fun retry() {
        writeFailed.value = false
        retryCount.update { it + 1 }
    }

    private fun write(operation: suspend () -> Unit) {
        viewModelScope.launch {
            try {
                operation()
            } catch (_: IOException) {
                writeFailed.value = true
            }
        }
    }
}
