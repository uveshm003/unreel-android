package io.github.uveshm003.unreel

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.core.view.WindowCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dagger.hilt.android.AndroidEntryPoint
import io.github.uveshm003.unreel.core.designsystem.theme.UnreelTheme
import io.github.uveshm003.unreel.feature.onboarding.OnboardingGate
import io.github.uveshm003.unreel.feature.settings.SettingsScreen
import io.github.uveshm003.unreel.feature.settings.SettingsViewModel
import io.github.uveshm003.unreel.ui.UnreelApp

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private val activity by viewModels<ActivityViewModel>()
    private val settings by viewModels<SettingsViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val state by settings.uiState.collectAsStateWithLifecycle()
            val darkTheme = state.appearance.theme.isDark(isSystemInDarkTheme())
            SideEffect {
                WindowCompat.getInsetsController(window, window.decorView).apply {
                    isAppearanceLightStatusBars = !darkTheme
                    isAppearanceLightNavigationBars = !darkTheme
                }
            }
            UnreelTheme(darkTheme = darkTheme, dynamicColor = state.appearance.dynamicColor) {
                OnboardingGate {
                    val summary by activity.summary.collectAsStateWithLifecycle()
                    androidx.compose.runtime.LaunchedEffect(activity) {
                        activity.errors.collect {
                            android.widget.Toast.makeText(
                                this@MainActivity,
                                io.github.uveshm003.unreel.feature.apps.R.string.apps_save_failed,
                                android.widget.Toast.LENGTH_LONG,
                            ).show()
                        }
                    }
                    androidx.compose.runtime.LaunchedEffect(activity) {
                        activity.exports.collect { ok ->
                            val message = if (ok) {
                                io.github.uveshm003.unreel.feature.settings.R.string.settings_export_done
                            } else {
                                io.github.uveshm003.unreel.feature.settings.R.string.settings_export_error
                            }
                            android.widget.Toast.makeText(
                                this@MainActivity,
                                message,
                                android.widget.Toast.LENGTH_LONG,
                            ).show()
                        }
                    }
                    UnreelApp(
                        summary = summary,
                        enableSchedule = activity::enable,
                        setTime = activity::setTime,
                        protectionStatus = { io.github.uveshm003.unreel.feature.apps.FeedPreview() },
                    ) {
                        SettingsScreen(
                            state,
                            BuildConfig.VERSION_NAME,
                            settings::setTheme,
                            settings::setDynamicColor,
                            settings::retry,
                            export = activity::export,
                        )
                    }
                }
            }
        }
    }
}
