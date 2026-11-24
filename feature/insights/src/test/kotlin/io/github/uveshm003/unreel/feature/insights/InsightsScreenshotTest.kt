package io.github.uveshm003.unreel.feature.insights

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import io.github.uveshm003.unreel.core.testing.ScreenshotSuite

class InsightsScreenshotTest : ScreenshotSuite("insights") {
    @Composable
    override fun Screen() {
        NavHost(rememberNavController(), startDestination = InsightsRoute) { insightsScreen() }
    }
}
