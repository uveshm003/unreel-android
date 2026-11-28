package io.github.uveshm003.unreel.feature.apps

import android.content.ActivityNotFoundException
import android.content.Intent
import android.provider.Settings
import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.uveshm003.unreel.core.data.PreviewCompatibility
import io.github.uveshm003.unreel.core.designsystem.component.SecondaryButton

@Composable
fun FeedPreview() = FeedPreviewContent()

@Composable
private fun FeedPreviewContent(viewModel: PreviewViewModel = hiltViewModel()) {
    val enabled by viewModel.enabled.collectAsStateWithLifecycle()
    val connected by viewModel.connected.collectAsStateWithLifecycle()
    val error by viewModel.error.collectAsStateWithLifecycle()
    val status by viewModel.status.collectAsStateWithLifecycle()
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { viewModel.refresh() }
    PreviewPanel(enabled, connected, error, status, viewModel::setEnabled)
}

@Composable
internal fun PreviewPanel(
    enabled: Boolean?,
    connected: Boolean,
    error: Boolean,
    status: PreviewStatus?,
    setEnabled: (Boolean) -> Unit,
) {
    var disclose by rememberSaveable { mutableStateOf(false) }
    val supported = status?.compatibility is PreviewCompatibility.Supported
    val context = LocalContext.current
    androidx.compose.material3.Surface(
        shape = androidx.compose.material3.MaterialTheme.shapes.large,
        color = androidx.compose.material3.MaterialTheme.colorScheme.surfaceContainerHigh,
    ) {
        Column(
            androidx.compose.ui.Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp),
        ) {
            Text(
                stringResource(R.string.preview_title),
                style = androidx.compose.material3.MaterialTheme.typography.titleLarge,
            )
            Text(compatibilityMessage(status?.compatibility))
            if (error) Text(stringResource(R.string.preview_error))
            when (enabled) {
                true -> {
                    if (supported) Text(stringResource(connectionMessage(connected, status?.serviceEnabled)))
                    if (supported && !connected && status?.serviceEnabled != true) {
                        SecondaryButton(stringResource(R.string.preview_settings), {
                            try {
                                context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
                            } catch (_: ActivityNotFoundException) {
                                Toast.makeText(context, R.string.preview_open_manually, Toast.LENGTH_LONG).show()
                            }
                        })
                    }
                    SecondaryButton(stringResource(R.string.preview_disable), { setEnabled(false) })
                }

                false -> if (supported) SecondaryButton(stringResource(R.string.preview_enable), { disclose = true })

                null -> Unit
            }
        }
    }
    if (disclose && supported && enabled == false) {
        AlertDialog(
            onDismissRequest = { disclose = false },
            title = { Text(stringResource(R.string.preview_title)) },
            text = { Text(stringResource(R.string.preview_disclosure)) },
            confirmButton = {
                TextButton(onClick = {
                    disclose = false
                    setEnabled(true)
                }) { Text(stringResource(R.string.preview_agree)) }
            },
            dismissButton = {
                TextButton(onClick = { disclose = false }) { Text(stringResource(R.string.preview_decline)) }
            },
        )
    }
}

@Composable
private fun compatibilityMessage(compatibility: PreviewCompatibility?): String = when (compatibility) {
    null -> stringResource(R.string.preview_checking)

    PreviewCompatibility.Missing -> stringResource(R.string.preview_youtube_missing)

    PreviewCompatibility.Unavailable -> stringResource(R.string.preview_unknown)

    is PreviewCompatibility.Supported -> stringResource(R.string.preview_support)

    is PreviewCompatibility.Unsupported -> if (compatibility.versionName.isNullOrBlank()) {
        stringResource(R.string.preview_unsupported_build, compatibility.versionCode)
    } else {
        stringResource(
            R.string.preview_unsupported_version,
            compatibility.versionName.orEmpty(),
            compatibility.versionCode,
        )
    }
}

private fun connectionMessage(connected: Boolean, enabled: Boolean?): Int = when {
    connected -> R.string.preview_connected
    enabled == true -> R.string.preview_waiting
    enabled == false -> R.string.preview_disconnected
    else -> R.string.preview_status_unknown
}
