package io.github.uveshm003.unreel.feature.insights

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import io.github.uveshm003.unreel.core.data.FeedDay
import io.github.uveshm003.unreel.core.testing.ScreenshotSuite
import java.time.LocalDate

class InsightsScreenshotTest : ScreenshotSuite("insights") {
    @Composable
    override fun Screen() {
        NavHost(rememberNavController(), startDestination = InsightsRoute) {
            insightsScreen {
                val today = LocalDate.of(2026, 11, 28)
                listOf(6, 2, 0, 4, 1, 3, 0).mapIndexed { index, minutes ->
                    FeedDay(today.minusDays(index.toLong()), minutes / 2, minutes * 60_000L)
                }
            }
        }
    }
}
