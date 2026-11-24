package io.github.uveshm003.unreel.feature.schedule

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import io.github.uveshm003.unreel.core.testing.ScreenshotSuite

class ScheduleScreenshotTest : ScreenshotSuite("schedule") {
    @Composable
    override fun Screen() {
        NavHost(rememberNavController(), startDestination = ScheduleRoute) { scheduleScreen() }
    }
}
