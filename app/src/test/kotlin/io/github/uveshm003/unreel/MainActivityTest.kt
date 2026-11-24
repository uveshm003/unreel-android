package io.github.uveshm003.unreel

import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.core.view.WindowCompat
import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.HiltTestApplication
import io.github.uveshm003.unreel.feature.apps.AppsRoute
import io.github.uveshm003.unreel.feature.home.HomeRoute
import io.github.uveshm003.unreel.feature.insights.InsightsRoute
import io.github.uveshm003.unreel.feature.schedule.ScheduleRoute
import io.github.uveshm003.unreel.feature.settings.SettingsRoute
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
@Config(application = HiltTestApplication::class)
class MainActivityTest {
    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun launches_and_shows_app_shell() {
        val description = composeRule.activity.getString(
            io.github.uveshm003.unreel.feature.home.R.string.home_description,
        )
        composeRule.onNodeWithText(description).assertIsDisplayed()
    }

    @Test
    fun allTabsOpenAndBackReturnsDirectlyHome() {
        val tabs = listOf(AppsRoute, ScheduleRoute, InsightsRoute, SettingsRoute, HomeRoute)
        for (route in tabs) {
            val label = composeRule.activity.getString(route.label)
            val tab = composeRule.onNode(hasText(label) and hasClickAction())
            tab.performClick().assertIsSelected()
            composeRule.onNode(hasText(label) and SemanticsMatcher.keyIsDefined(SemanticsProperties.Heading))
                .assertIsDisplayed()
        }
        composeRule.onNode(
            hasText(composeRule.activity.getString(InsightsRoute.label)) and hasClickAction(),
        ).performClick()
        composeRule.runOnIdle { composeRule.activity.onBackPressedDispatcher.onBackPressed() }
        composeRule.onNode(
            hasText(composeRule.activity.getString(HomeRoute.label)) and hasClickAction(),
        ).assertIsSelected()
    }

    @Test
    @Config(qualifiers = "night")
    fun darkThemeUsesLightSystemBarIcons() = assertLightBarAppearance(false)

    @Test
    @Config(qualifiers = "notnight")
    fun lightThemeUsesDarkSystemBarIcons() = assertLightBarAppearance(true)

    private fun assertLightBarAppearance(expected: Boolean) {
        composeRule.runOnIdle {
            val window = composeRule.activity.window
            val controller = WindowCompat.getInsetsController(window, window.decorView)
            assertEquals(expected, controller.isAppearanceLightStatusBars)
            assertEquals(expected, controller.isAppearanceLightNavigationBars)
        }
    }
}
