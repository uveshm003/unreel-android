package io.github.uveshm003.unreel.ui

import android.content.Context
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.unit.Density
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.takahirom.roborazzi.captureRoboImage
import io.github.uveshm003.unreel.R
import io.github.uveshm003.unreel.core.designsystem.theme.UnreelTheme
import org.junit.Assert.assertFalse
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

    @Test
    @Config(qualifiers = "w480dp-h220dp-land-notnight-mdpi")
    fun shortWindow_largeFont_canReadFullDescriptionAndReturnToHeading() {
        composeRule.setContent {
            CompositionLocalProvider(LocalDensity provides Density(LocalDensity.current.density, fontScale = 2f)) {
                UnreelTheme(darkTheme = false, dynamicColor = false) { UnreelApp() }
            }
        }
        val context = ApplicationProvider.getApplicationContext<Context>()
        val description = composeRule.onNodeWithText(context.getString(R.string.app_shell_placeholder))
        val layouts = mutableListOf<TextLayoutResult>()
        description.performSemanticsAction(SemanticsActions.GetTextLayoutResult) { it(layouts) }
        assertFalse("The description must lay out every line", layouts.single().didOverflowHeight)
        description.performScrollTo().assertIsDisplayed()
        composeRule.onRoot().captureRoboImage("src/test/screenshots/unreel_app_short_window_large_font.png")
        composeRule.onNodeWithText(context.getString(R.string.app_name)).performScrollTo().assertIsDisplayed()
    }

    private fun capture(name: String, darkTheme: Boolean) {
        composeRule.setContent {
            UnreelTheme(darkTheme = darkTheme, dynamicColor = false) { UnreelApp() }
        }
        composeRule.onRoot().captureRoboImage("src/test/screenshots/$name.png")
    }
}
