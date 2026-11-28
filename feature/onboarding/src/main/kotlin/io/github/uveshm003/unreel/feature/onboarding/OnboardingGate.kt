package io.github.uveshm003.unreel.feature.onboarding

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.uveshm003.unreel.core.designsystem.component.EmptyState
import io.github.uveshm003.unreel.core.designsystem.component.PrimaryButton
import io.github.uveshm003.unreel.core.designsystem.component.SecondaryButton
import io.github.uveshm003.unreel.core.model.SetupStep

@Composable
fun OnboardingGate(
    modifier: Modifier = Modifier,
    viewModel: OnboardingViewModel = hiltViewModel(),
    content: @Composable () -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    Box(modifier = modifier.fillMaxSize(), propagateMinConstraints = true) {
        when (val current = state) {
            SetupState.Loading -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }

            SetupState.StorageError -> SetupFrame {
                Text(stringResource(R.string.setup_storage_error))
                PrimaryButton(stringResource(R.string.setup_retry), viewModel::retry)
            }

            is SetupState.Ready -> if (current.step == SetupStep.Complete) {
                content()
            } else {
                key(current.step) {
                    SetupScreen(current.step, viewModel::advance, viewModel::back, viewModel::skipTips)
                }
            }
        }
    }
}

@Composable
internal fun SetupScreen(
    step: SetupStep,
    advance: (SetupStep) -> Unit,
    back: (SetupStep) -> Unit,
    skipTips: () -> Unit,
) {
    val (title, body) = when (step) {
        SetupStep.Welcome -> R.string.setup_welcome to R.string.setup_welcome_body
        SetupStep.Disclosure -> R.string.setup_disclosure to R.string.setup_disclosure_body
        SetupStep.Service -> R.string.setup_service to R.string.setup_service_body
        SetupStep.Guides -> R.string.setup_guides to R.string.setup_guides_body
        SetupStep.Complete -> return
    }
    BackHandler(enabled = step != SetupStep.Welcome) { back(step) }
    SetupFrame {
        Text(
            stringResource(R.string.setup_progress, step.ordinal + 1, SetupStep.Complete.ordinal),
            style = MaterialTheme.typography.labelLarge,
        )
        EmptyState(stringResource(title), stringResource(body))
        if (step == SetupStep.Guides) BatteryGuidance()
        val action = if (step == SetupStep.Guides) R.string.setup_finish else R.string.setup_next
        PrimaryButton(stringResource(action), { advance(step) })
        if (step == SetupStep.Service) SecondaryButton(stringResource(R.string.setup_skip_tips), skipTips)
        if (step != SetupStep.Welcome) SecondaryButton(stringResource(R.string.setup_back), { back(step) })
    }
}

@Composable
private fun SetupFrame(content: @Composable () -> Unit) {
    Surface(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier.safeDrawingPadding().verticalScroll(rememberScrollState()).padding(24.dp),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                modifier = Modifier.widthIn(max = 520.dp).fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) { content() }
        }
    }
}
