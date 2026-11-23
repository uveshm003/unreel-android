package io.github.uveshm003.unreel.feature.settings

import androidx.compose.runtime.Composable
import io.github.uveshm003.unreel.core.testing.ScreenshotSuite

class SettingsScreenshotTest : ScreenshotSuite("settings") {
    @Composable
    override fun Screen() = SettingsScreen()
}
