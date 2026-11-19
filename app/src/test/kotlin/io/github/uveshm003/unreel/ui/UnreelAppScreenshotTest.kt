package io.github.uveshm003.unreel.ui

import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.takahirom.roborazzi.captureRoboImage
import io.github.uveshm003.unreel.core.designsystem.theme.UnreelTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

/** Bootstrap screenshot test. The M0 core:testing issue replaces this with shared Roborazzi helpers. */
@RunWith(AndroidJUnit4::class)
class UnreelAppScreenshotTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    @Config(qualifiers = "w411dp-h891dp-notnight-xxhdpi")
    fun unreelApp_phone_light() = capture("unreel_app_phone_light", darkTheme = false)

    @Test
    @Config(qualifiers = "w411dp-h891dp-night-xxhdpi")
    fun unreelApp_phone_dark() = capture("unreel_app_phone_dark", darkTheme = true)

    @Test
    @Config(qualifiers = "w1280dp-h800dp-notnight-xhdpi")
    fun unreelApp_tablet_light() = capture("unreel_app_tablet_light", darkTheme = false)

    @Test
    @Config(qualifiers = "w1280dp-h800dp-night-xhdpi")
    fun unreelApp_tablet_dark() = capture("unreel_app_tablet_dark", darkTheme = true)

    private fun capture(name: String, darkTheme: Boolean) {
        composeRule.setContent {
            UnreelTheme(darkTheme = darkTheme, dynamicColor = false) { UnreelApp() }
        }
        composeRule.onRoot().captureRoboImage("src/test/screenshots/$name.png")
    }
}
