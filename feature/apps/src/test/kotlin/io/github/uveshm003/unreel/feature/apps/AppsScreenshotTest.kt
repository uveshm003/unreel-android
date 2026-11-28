package io.github.uveshm003.unreel.feature.apps

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextReplacement
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import io.github.uveshm003.unreel.core.model.AppProtection
import io.github.uveshm003.unreel.core.model.ProtectedApp
import io.github.uveshm003.unreel.core.model.ProtectionLevel
import io.github.uveshm003.unreel.core.testing.ScreenshotSuite
import org.junit.Test
import org.robolectric.annotation.Config

class AppsScreenshotTest : ScreenshotSuite("apps") {
    @Composable
    override fun Screen() {
        var policies by remember {
            mutableStateOf(ProtectedApp.entries.associate { it.packageName to AppProtection(it.packageName) })
        }
        fun update(app: ProtectedApp, transform: (AppProtection) -> AppProtection) {
            policies = policies + (app.packageName to transform(policies.getValue(app.packageName)))
        }
        NavHost(rememberNavController(), startDestination = AppsRoute) {
            appsScreen {
                AppsScreen(
                    AppsUiState(policies, setOf(ProtectedApp.YouTube)),
                    { app, level -> update(app) { it.copy(level = level) } },
                    { app, minutes -> update(app) { it.copy(dailyBudgetMinutes = minutes) } },
                    { app, enabled -> update(app) { it.copy(allowOneBlockScroll = enabled) } },
                    {},
                )
            }
        }
    }

    @Test
    @Config(qualifiers = PHONE_LIGHT)
    fun largeFont() = capture("phone_light_large_font", darkTheme = false, fontScale = 2f)

    @Test
    fun budgetEditorIsAvailableOnlyForBudgetAndRejectsInvalidMinutes() {
        setScreenshotContent(false)
        composeRule.onNodeWithText("Instagram daily budget").assertDoesNotExist()
        composeRule.onNodeWithText("Instagram protection").performScrollTo().performClick()
        composeRule.onNodeWithText("Budget").performClick()
        composeRule.onNodeWithText("Instagram daily budget").performScrollTo().performClick()
        val field = composeRule.onNodeWithText("Minutes (1–240)")
        field.performTextReplacement("241")
        composeRule.onNodeWithText("Save").assertIsNotEnabled()
        field.performTextReplacement("42")
        composeRule.onNodeWithText("Save").performClick()
        composeRule.onNodeWithText("42 min").assertExists()
        composeRule.onNodeWithText("YouTube daily budget").assertDoesNotExist()
    }
}
