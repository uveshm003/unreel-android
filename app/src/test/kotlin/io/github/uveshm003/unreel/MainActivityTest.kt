package io.github.uveshm003.unreel

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.core.view.WindowCompat
import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.HiltTestApplication
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
        val tagline = composeRule.activity.getString(R.string.app_tagline)
        composeRule.onNodeWithText(tagline).assertIsDisplayed()
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
