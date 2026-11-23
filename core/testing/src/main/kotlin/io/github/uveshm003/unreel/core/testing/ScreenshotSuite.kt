package io.github.uveshm003.unreel.core.testing

import android.view.View
import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.unit.Density
import androidx.core.view.drawToBitmap
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.takahirom.roborazzi.captureRoboImage
import io.github.uveshm003.unreel.core.designsystem.theme.UnreelTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

/**
 * Renders [Screen] with fixed phone and tablet configurations in light and dark mode.
 * For example, extend `ScreenshotSuite("home")` and override `@Composable fun Screen() { HomeScreen() }`.
 * The four inherited tests write `src/test/screenshots/home_<variant>.png` in the consuming module.
 * To add a large-text case, annotate another test with `@Config(qualifiers = PHONE_LIGHT)` and call
 * `capture("phone_light_large_font", darkTheme = false, fontScale = 2f)`.
 */
@RunWith(AndroidJUnit4::class)
abstract class ScreenshotSuite(private val name: String) {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Composable
    protected abstract fun Screen()

    @Test
    @Config(qualifiers = PHONE_LIGHT)
    fun phoneLight() = capture("phone_light", darkTheme = false)

    @Test
    @Config(qualifiers = "w411dp-h891dp-night-xxhdpi")
    fun phoneDark() = capture("phone_dark", darkTheme = true)

    @Test
    @Config(qualifiers = "w1280dp-h800dp-notnight-xhdpi")
    fun tabletLight() = capture("tablet_light", darkTheme = false)

    @Test
    @Config(qualifiers = "w1280dp-h800dp-night-xhdpi")
    fun tabletDark() = capture("tablet_dark", darkTheme = true)

    protected fun capture(variant: String, darkTheme: Boolean, fontScale: Float = 1f) {
        setScreenshotContent(darkTheme, fontScale)
        captureCurrentScreen(variant)
    }

    protected fun setScreenshotContent(darkTheme: Boolean, fontScale: Float = 1f) {
        composeRule.setContent {
            CompositionLocalProvider(LocalDensity provides Density(LocalDensity.current.density, fontScale)) {
                UnreelTheme(darkTheme = darkTheme, dynamicColor = false) { Screen() }
            }
        }
    }

    protected fun captureCurrentScreen(variant: String) {
        // Draw the settled content view synchronously. A window capture can still contain the previous frame.
        val bitmap = composeRule.runOnIdle {
            composeRule.activity.findViewById<View>(android.R.id.content).drawToBitmap()
        }
        bitmap.captureRoboImage("src/test/screenshots/${name}_$variant.png")
    }

    companion object {
        const val PHONE_LIGHT = "w411dp-h891dp-notnight-xxhdpi"
    }
}
