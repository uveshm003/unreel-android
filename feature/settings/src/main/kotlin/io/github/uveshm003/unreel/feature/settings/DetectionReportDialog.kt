package io.github.uveshm003.unreel.feature.settings

import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
internal fun ReportProblemAction(version: String) {
    var open by rememberSaveable { mutableStateOf(false) }
    TextButton(onClick = { open = true }) { Text(stringResource(R.string.settings_report)) }
    if (open) DetectionReportDialog(version, onDismiss = { open = false })
}

@Composable
internal fun DetectionReportDialog(version: String, onDismiss: () -> Unit) {
    val context = LocalContext.current
    var app by rememberSaveable { mutableStateOf<ReportApp?>(null) }
    var noBrowser by rememberSaveable { mutableStateOf(false) }
    val report by produceState<DetectionReport?>(null, app, version, context) {
        val selected = app
        value = selected?.let {
            val installed = withContext(Dispatchers.IO) { it.installedVersion(context.packageManager) }
            DetectionReport(
                it,
                installed,
                version,
                "${Build.MANUFACTURER} ${Build.MODEL}, Android ${Build.VERSION.RELEASE}",
            )
        }
    }
    val current = report?.takeIf { it.app == app }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.settings_report)) },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                Text(stringResource(R.string.report_intro))
                ReportAppChoices(app) {
                    app = it
                    noBrowser = false
                }
                if (current != null) {
                    Text(
                        stringResource(
                            R.string.report_app_version,
                            current.appVersion ?: stringResource(R.string.report_unknown),
                        ),
                    )
                    Text(stringResource(R.string.report_unreel_version, current.unreelVersion))
                    Text(current.device)
                }
                if (noBrowser) Text(stringResource(R.string.report_no_browser))
            }
        },
        confirmButton = {
            TextButton(enabled = current != null, onClick = {
                if (current != null) {
                    if (noBrowser) {
                        context.copyReportLink(current)
                        onDismiss()
                    } else if (context.openReport(current)) {
                        onDismiss()
                    } else {
                        noBrowser = true
                    }
                }
            }) { Text(stringResource(if (noBrowser) R.string.report_copy else R.string.report_open)) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(android.R.string.cancel)) } },
    )
}

@Composable
private fun ReportAppChoices(current: ReportApp?, choose: (ReportApp) -> Unit) {
    Column(Modifier.selectableGroup()) {
        ReportApp.entries.forEach { app ->
            Row(
                modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)
                    .selectable(current == app, role = Role.RadioButton) { choose(app) },
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                RadioButton(selected = current == app, onClick = null)
                Text(if (app == ReportApp.Other) stringResource(R.string.report_other) else app.formValue)
            }
        }
    }
}

private fun Context.copyReportLink(report: DetectionReport) {
    val clipboard = getSystemService(ClipboardManager::class.java)
    val label = getString(R.string.settings_report)
    clipboard.setPrimaryClip(ClipData.newPlainText(label, report.issueUrl().toString()))
}

private fun Context.openReport(report: DetectionReport): Boolean = try {
    startActivity(Intent(Intent.ACTION_VIEW, report.issueUrl()))
    true
} catch (_: ActivityNotFoundException) {
    false
} catch (_: SecurityException) {
    false
}
