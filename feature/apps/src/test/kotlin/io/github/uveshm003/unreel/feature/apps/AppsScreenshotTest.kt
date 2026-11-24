package io.github.uveshm003.unreel.feature.apps

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import io.github.uveshm003.unreel.core.testing.ScreenshotSuite

class AppsScreenshotTest : ScreenshotSuite("apps") {
    @Composable
    override fun Screen() {
        NavHost(rememberNavController(), startDestination = AppsRoute) { appsScreen() }
    }
}
