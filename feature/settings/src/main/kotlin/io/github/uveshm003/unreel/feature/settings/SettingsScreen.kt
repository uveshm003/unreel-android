package io.github.uveshm003.unreel.feature.settings

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.core.os.LocaleListCompat
import io.github.uveshm003.unreel.core.data.ThemeMode
import io.github.uveshm003.unreel.core.designsystem.component.SettingsRow

private const val PROJECT_URL = "https://github.com/uveshm003/unreel-android"

@Composable
fun SettingsScreen(
    state: SettingsUiState,
    version: String,
    setTheme: (ThemeMode) -> Unit,
    setDynamicColor: (Boolean) -> Unit,
    retry: () -> Unit,
    modifier: Modifier = Modifier,
    export: (android.net.Uri) -> Unit = {},
) {
    Surface(modifier = modifier.fillMaxSize()) {
        Box(contentAlignment = Alignment.TopCenter) {
            Column(
                modifier = Modifier.widthIn(
                    max = 640.dp,
                ).fillMaxWidth().verticalScroll(rememberScrollState()).padding(16.dp),
            ) {
                Text(
                    stringResource(R.string.settings_label),
                    modifier = Modifier.semantics { heading() },
                    style = MaterialTheme.typography.headlineMedium,
                )
                if (state.storageError) {
                    Text(stringResource(R.string.settings_storage_error))
                    TextButton(onClick = retry) { Text(stringResource(R.string.settings_retry)) }
                }
                PreferencePicker(
                    R.string.settings_theme,
                    state.appearance.theme,
                    ThemeMode.entries,
                    { mode ->
                        when (mode) {
                            ThemeMode.System -> R.string.settings_system
                            ThemeMode.Light -> R.string.settings_light
                            ThemeMode.Dark -> R.string.settings_dark
                        }
                    },
                    setTheme,
                )
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    SettingsRow(
                        stringResource(R.string.settings_dynamic),
                        state.appearance.dynamicColor,
                        setDynamicColor,
                    )
                }
                LanguageSetting()
                AboutSettings(version)
                SectionHeading(R.string.settings_data)
                ReportProblemAction(version)
                DataActions(export)
            }
        }
    }
}

@Composable
private fun LanguageSetting() {
    val language = AppCompatDelegate.getApplicationLocales()[0]?.language.orEmpty()
    PreferencePicker(
        R.string.settings_language,
        language,
        listOf("", "en", "hi"),
        { tag ->
            when (tag) {
                "en" -> R.string.settings_english
                "hi" -> R.string.settings_hindi
                else -> R.string.settings_system
            }
        },
    ) { tag -> AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(tag)) }
}

@Composable
private fun <T> PreferencePicker(title: Int, value: T, options: List<T>, label: (T) -> Int, choose: (T) -> Unit) {
    var open by rememberSaveable { mutableStateOf(false) }
    SettingsRow(stringResource(title), stringResource(label(value)), onClick = { open = true })
    if (open) {
        AlertDialog(
            onDismissRequest = { open = false },
            title = { Text(stringResource(title)) },
            text = {
                Column(Modifier.selectableGroup().verticalScroll(rememberScrollState())) {
                    options.forEach { option ->
                        Row(
                            modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)
                                .selectable(value == option, role = Role.RadioButton) {
                                    open = false
                                    choose(option)
                                },
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            RadioButton(selected = value == option, onClick = null)
                            Text(stringResource(label(option)))
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { open = false }) { Text(stringResource(android.R.string.cancel)) }
            },
        )
    }
}

@Composable
private fun AboutSettings(version: String) {
    val context = LocalContext.current
    Column {
        SectionHeading(R.string.settings_about)
        Text(stringResource(R.string.settings_version, version), modifier = Modifier.padding(horizontal = 16.dp))
        listOf(
            R.string.settings_license to "$PROJECT_URL/blob/main/LICENSE",
            R.string.settings_source to PROJECT_URL,
            R.string.settings_privacy to "$PROJECT_URL#privacy",
        ).forEach { (label, url) ->
            TextButton(onClick = { context.openPage(url) }) { Text(stringResource(label)) }
        }
    }
}

@Composable
private fun SectionHeading(label: Int) {
    Text(
        stringResource(label),
        Modifier.padding(top = 16.dp, bottom = 4.dp).semantics { heading() },
        style = MaterialTheme.typography.titleLarge,
    )
}

private fun Context.openPage(url: String) {
    try {
        startActivity(Intent(Intent.ACTION_VIEW, url.toUri()))
    } catch (_: ActivityNotFoundException) {
        Toast.makeText(this, R.string.settings_browser_error, Toast.LENGTH_LONG).show()
    } catch (_: SecurityException) {
        Toast.makeText(this, R.string.settings_browser_error, Toast.LENGTH_LONG).show()
    }
}
