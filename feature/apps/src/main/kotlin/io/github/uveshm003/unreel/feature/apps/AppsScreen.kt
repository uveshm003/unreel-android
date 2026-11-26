package io.github.uveshm003.unreel.feature.apps

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.uveshm003.unreel.core.designsystem.component.SettingsRow
import io.github.uveshm003.unreel.core.designsystem.component.UnreelCard
import io.github.uveshm003.unreel.core.model.AppProtection
import io.github.uveshm003.unreel.core.model.ProtectedApp
import io.github.uveshm003.unreel.core.model.ProtectionLevel

@Composable
internal fun AppsDestination(viewModel: AppsViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { viewModel.refreshInstalled() }
    LaunchedEffect(viewModel, context) {
        viewModel.errors.collect { Toast.makeText(context, it, Toast.LENGTH_LONG).show() }
    }
    AppsScreen(state, viewModel::setLevel, viewModel::setBudget, viewModel::setAllowOne, viewModel::retry) {
        FeedPreview()
    }
}

@Composable
internal fun AppsScreen(
    state: AppsUiState,
    setLevel: (ProtectedApp, ProtectionLevel) -> Unit,
    setBudget: (ProtectedApp, Int) -> Unit,
    setAllowOne: (ProtectedApp, Boolean) -> Unit,
    retry: () -> Unit,
    preview: @Composable () -> Unit = {},
) {
    Surface(modifier = Modifier.fillMaxSize()) {
        Box(contentAlignment = Alignment.TopCenter) {
            Column(
                modifier = Modifier.widthIn(
                    max = 640.dp,
                ).fillMaxWidth().verticalScroll(rememberScrollState()).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    stringResource(R.string.apps_label),
                    Modifier.semantics {
                        heading()
                    },
                    style = MaterialTheme.typography.headlineMedium,
                )
                preview()
                Text(stringResource(R.string.apps_description))
                when {
                    state.readFailed -> {
                        Text(stringResource(R.string.apps_load_failed))
                        TextButton(onClick = retry) { Text(stringResource(R.string.apps_retry)) }
                    }

                    state.policies.isEmpty() -> CircularProgressIndicator()

                    else -> ProtectedApp.entries.forEach { app ->
                        val policy = state.policies.getValue(app.packageName)
                        AppPolicyCard(
                            app,
                            policy,
                            state.installed?.contains(app),
                            { setLevel(app, it) },
                            { setBudget(app, it) },
                            { setAllowOne(app, it) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AppPolicyCard(
    app: ProtectedApp,
    policy: AppProtection,
    installed: Boolean?,
    setLevel: (ProtectionLevel) -> Unit,
    setBudget: (Int) -> Unit,
    setAllowOne: (Boolean) -> Unit,
) {
    val name = app.name
    var editingLevel by rememberSaveable(app) { mutableStateOf(false) }
    var editingBudget by rememberSaveable(app) { mutableStateOf(false) }
    UnreelCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            val icon = if (app == ProtectedApp.Instagram) R.drawable.ic_instagram else R.drawable.ic_youtube
            Image(painterResource(icon), contentDescription = null, modifier = Modifier.size(32.dp))
            Column(modifier = Modifier.semantics(mergeDescendants = true) { heading() }) {
                Text(name, style = MaterialTheme.typography.titleMedium)
                val status = when (installed) {
                    true -> R.string.apps_installed
                    false -> R.string.apps_not_installed
                    null -> R.string.apps_install_unknown
                }
                Text(stringResource(status), style = MaterialTheme.typography.bodySmall)
            }
        }
        SettingsRow(stringResource(R.string.apps_level, name), stringResource(policy.level.label), {
            editingLevel = true
        })
        if (policy.level == ProtectionLevel.Budget) {
            SettingsRow(
                stringResource(
                    R.string.apps_budget,
                    name,
                ),
                stringResource(R.string.apps_minutes, policy.dailyBudgetMinutes),
                {
                    editingBudget =
                        true
                },
            )
        }
        SettingsRow(
            stringResource(R.string.apps_allow_one, name),
            policy.allowOneBlockScroll,
            setAllowOne,
            subtitle = stringResource(R.string.apps_allow_one_body),
        )
    }
    if (editingLevel) {
        LevelPicker(name, policy.level, { editingLevel = false }) {
            editingLevel = false
            setLevel(it)
        }
    }
    if (editingBudget) {
        BudgetEditor(name, policy.dailyBudgetMinutes, { editingBudget = false }) {
            editingBudget = false
            setBudget(it)
        }
    }
}

private val ProtectionLevel.label: Int get() = when (this) {
    ProtectionLevel.Off -> R.string.apps_off
    ProtectionLevel.Pause -> R.string.apps_pause
    ProtectionLevel.Budget -> R.string.apps_level_budget
    ProtectionLevel.HardBlock -> R.string.apps_block
}

@Composable
private fun LevelPicker(
    name: String,
    selected: ProtectionLevel,
    dismiss: () -> Unit,
    choose: (ProtectionLevel) -> Unit,
) {
    AlertDialog(
        onDismissRequest = dismiss,
        title = { Text(stringResource(R.string.apps_level, name)) },
        text = {
            Column(Modifier.selectableGroup().verticalScroll(rememberScrollState())) {
                ProtectionLevel.entries.forEach { level ->
                    Row(
                        Modifier.fillMaxWidth().heightIn(
                            min = 48.dp,
                        ).selectable(level == selected, role = Role.RadioButton) {
                            choose(level)
                        }.padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        RadioButton(level == selected, onClick = null)
                        Column {
                            Text(stringResource(level.label))
                            val description = when (level) {
                                ProtectionLevel.Off -> R.string.apps_off_body
                                ProtectionLevel.Pause -> R.string.apps_pause_body
                                ProtectionLevel.Budget -> R.string.apps_budget_body
                                ProtectionLevel.HardBlock -> R.string.apps_block_body
                            }
                            Text(stringResource(description), style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = dismiss) { Text(stringResource(android.R.string.cancel)) } },
    )
}

@Composable
private fun BudgetEditor(name: String, initial: Int, dismiss: () -> Unit, save: (Int) -> Unit) {
    var text by rememberSaveable { mutableStateOf(initial.toString()) }
    val minutes = text.toIntOrNull()?.takeIf { it in AppProtection.budgetRange }
    AlertDialog(
        onDismissRequest = dismiss,
        title = { Text(stringResource(R.string.apps_budget, name)) },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                singleLine = true,
                label = { Text(stringResource(R.string.apps_budget_range)) },
                isError = minutes == null,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            )
        },
        confirmButton = {
            TextButton(enabled = minutes != null, onClick = {
                minutes?.let(save)
            }) { Text(stringResource(R.string.apps_save)) }
        },
        dismissButton = { TextButton(onClick = dismiss) { Text(stringResource(android.R.string.cancel)) } },
    )
}
