package io.github.uveshm003.unreel.feature.apps

import androidx.compose.runtime.Composable
import io.github.uveshm003.unreel.core.testing.ScreenshotSuite

class AppsScreenshotTest : ScreenshotSuite("apps") {
    @Composable
    override fun Screen() = AppsScreen()
}
