package io.github.uveshm003.unreel

import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.core.view.WindowCompat
import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.HiltTestApplication
import io.github.uveshm003.unreel.feature.home.R.string as HomeStrings
import io.github.uveshm003.unreel.feature.onboarding.R.string as SetupStrings
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
    fun freshInstallResumesSetupAndCompletedSetupOpensHome() {
        see(SetupStrings.setup_welcome).assertIsDisplayed()
        see(SetupStrings.setup_next).performClick()
        see(SetupStrings.setup_disclosure).assertIsDisplayed()
        composeRule.activityRule.scenario.recreate()
        see(SetupStrings.setup_disclosure).assertIsDisplayed()
        composeRule.runOnIdle { composeRule.activity.onBackPressedDispatcher.onBackPressed() }
        see(SetupStrings.setup_welcome).assertIsDisplayed()
        see(SetupStrings.setup_next).performClick()
        see(SetupStrings.setup_disclosure)
        see(SetupStrings.setup_next).performClick()
        see(SetupStrings.setup_service)
        see(SetupStrings.setup_next).performClick()
        see(SetupStrings.setup_guides)
        see(SetupStrings.setup_finish).performClick()
        see(HomeStrings.home_description).assertIsDisplayed()
        composeRule.activityRule.scenario.recreate()
        see(HomeStrings.home_description).assertIsDisplayed()
    }

    private fun see(resource: Int): SemanticsNodeInteraction {
        val text = composeRule.activity.getString(resource)
        composeRule.waitUntil(timeoutMillis = 10_000) {
            composeRule.onAllNodesWithText(text).fetchSemanticsNodes().isNotEmpty()
        }
        return composeRule.onNodeWithText(text)
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
