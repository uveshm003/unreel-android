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
                    UnreelApp {
                        SettingsScreen(
                            state,
                            BuildConfig.VERSION_NAME,
                            settings::setTheme,
                            settings::setDynamicColor,
                            settings::retry,
                        )
                    }
                }
            }
        }
    }
}
