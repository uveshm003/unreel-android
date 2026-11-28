package io.github.uveshm003.unreel.feature.settings

import android.app.ActivityManager
import android.content.ActivityNotFoundException
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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

@Composable
internal fun DataActions(export: (Uri) -> Unit) {
    val context = LocalContext.current
    var confirm by rememberSaveable { mutableStateOf(false) }
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) {
        it?.let(export)
    }
    androidx.compose.foundation.layout.Column {
        TextButton(onClick = {
            try {
                picker.launch("unreel.json")
            } catch (_: ActivityNotFoundException) {
                Toast.makeText(context, R.string.settings_export_error, Toast.LENGTH_LONG).show()
            }
        }) { Text(stringResource(R.string.settings_export)) }
        TextButton(onClick = { confirm = true }) { Text(stringResource(R.string.settings_reset)) }
        if (confirm) {
            AlertDialog(
                onDismissRequest = { confirm = false },
                title = { Text(stringResource(R.string.settings_reset)) },
                text = { Text(stringResource(R.string.settings_reset_detail)) },
                dismissButton = {
                    TextButton(onClick = { confirm = false }) {
                        Text(stringResource(android.R.string.cancel))
                    }
                },
                confirmButton = {
                    TextButton(onClick = {
                        confirm = false
                        if (context.getSystemService(ActivityManager::class.java)?.clearApplicationUserData() != true) {
                            Toast.makeText(context, R.string.settings_reset_error, Toast.LENGTH_LONG).show()
                        }
                    }) { Text(stringResource(R.string.settings_reset)) }
                },
            )
        }
    }
}
