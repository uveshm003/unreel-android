package io.github.uveshm003.unreel.feature.insights

import androidx.compose.runtime.Composable
import io.github.uveshm003.unreel.core.testing.ScreenshotSuite

class InsightsScreenshotTest : ScreenshotSuite("insights") {
    @Composable
    override fun Screen() = InsightsScreen()
}
