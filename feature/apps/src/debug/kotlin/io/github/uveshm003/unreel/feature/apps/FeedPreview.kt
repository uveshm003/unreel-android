package io.github.uveshm003.unreel.feature.apps

import android.content.ActivityNotFoundException
import android.content.Intent
import android.provider.Settings
import android.widget.Toast
import androidx.compose.foundation.layout.Column
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
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.uveshm003.unreel.core.designsystem.component.SecondaryButton

@Composable
internal fun FeedPreview(viewModel: PreviewViewModel = hiltViewModel()) {
    val enabled by viewModel.enabled.collectAsStateWithLifecycle()
    val connected by viewModel.connected.collectAsStateWithLifecycle()
    val error by viewModel.error.collectAsStateWithLifecycle()
    PreviewPanel(enabled, connected, error, viewModel::setEnabled)
}

@Composable
internal fun PreviewPanel(enabled: Boolean?, connected: Boolean, error: Boolean, setEnabled: (Boolean) -> Unit) {
    var disclose by rememberSaveable { mutableStateOf(false) }
    val context = LocalContext.current
    Column {
        Text(stringResource(R.string.preview_title))
        Text(stringResource(R.string.preview_support))
        if (error) Text(stringResource(R.string.preview_error))
        when (enabled) {
            true -> {
                Text(stringResource(if (connected) R.string.preview_connected else R.string.preview_disconnected))
                if (!connected) {
                    SecondaryButton(stringResource(R.string.preview_settings), {
                        try {
                            context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
                        } catch (
                            _: ActivityNotFoundException,
                        ) {
                            Toast.makeText(context, R.string.preview_open_manually, Toast.LENGTH_LONG).show()
                        }
                    })
                }
                SecondaryButton(stringResource(R.string.preview_disable), { setEnabled(false) })
            }

            false -> SecondaryButton(stringResource(R.string.preview_enable), { disclose = true })

            null -> Unit
        }
    }
    if (disclose) {
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
