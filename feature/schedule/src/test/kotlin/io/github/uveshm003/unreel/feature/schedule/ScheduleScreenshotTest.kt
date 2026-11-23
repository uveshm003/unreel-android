package io.github.uveshm003.unreel.feature.schedule

import androidx.compose.runtime.Composable
import io.github.uveshm003.unreel.core.testing.ScreenshotSuite

class ScheduleScreenshotTest : ScreenshotSuite("schedule") {
    @Composable
    override fun Screen() = ScheduleScreen()
}
