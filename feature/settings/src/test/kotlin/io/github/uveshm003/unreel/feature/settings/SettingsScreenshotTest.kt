package io.github.uveshm003.unreel.feature.settings

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import io.github.uveshm003.unreel.core.testing.ScreenshotSuite

class SettingsScreenshotTest : ScreenshotSuite("settings") {
    @Composable
    override fun Screen() {
        NavHost(rememberNavController(), startDestination = SettingsRoute) { settingsScreen() }
    }
}
