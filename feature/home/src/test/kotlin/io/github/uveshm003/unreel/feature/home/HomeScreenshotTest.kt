package io.github.uveshm003.unreel.feature.home

import androidx.compose.runtime.Composable
import io.github.uveshm003.unreel.core.testing.ScreenshotSuite

class HomeScreenshotTest : ScreenshotSuite("home") {
    @Composable
    override fun Screen() = HomeScreen()
}
