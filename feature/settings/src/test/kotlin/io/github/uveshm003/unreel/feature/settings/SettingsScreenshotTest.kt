package io.github.uveshm003.unreel.feature.settings

import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import io.github.uveshm003.unreel.core.testing.ScreenshotSuite
import org.junit.Assert.assertEquals
import org.junit.Test
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

class SettingsScreenshotTest : ScreenshotSuite("settings") {
    @Composable
    override fun Screen() {
        NavHost(rememberNavController(), startDestination = SettingsRoute) {
            settingsScreen { SettingsScreen(SettingsUiState(), "1.2.3", {}, {}, {}) }
        }
    }

    @Test
    @Config(qualifiers = PHONE_LIGHT)
    fun largeFont() = capture("phone_light_large_font", darkTheme = false, fontScale = 2f)

    @Test
    @Config(sdk = [30])
    fun android11HidesWallpaperColoursAndOpensPublishedPrivacyInformation() {
        setScreenshotContent(darkTheme = false)
        val activity = composeRule.activity
        composeRule.onNodeWithText(activity.getString(R.string.settings_dynamic)).assertDoesNotExist()
        composeRule.onNodeWithText(activity.getString(R.string.settings_privacy)).performScrollTo().performClick()
        val intent = shadowOf(activity).nextStartedActivity
        assertEquals(Intent.ACTION_VIEW, intent.action)
        assertEquals("https://github.com/uveshm003/unreel-android#privacy", intent.dataString)
    }
}
