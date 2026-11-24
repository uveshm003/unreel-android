package io.github.uveshm003.unreel.feature.home

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import io.github.uveshm003.unreel.core.testing.ScreenshotSuite

class HomeScreenshotTest : ScreenshotSuite("home") {
    @Composable
    override fun Screen() {
        NavHost(rememberNavController(), startDestination = HomeRoute) { homeScreen() }
    }
}
