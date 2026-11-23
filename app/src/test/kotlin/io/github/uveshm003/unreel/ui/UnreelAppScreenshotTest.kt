package io.github.uveshm003.unreel.ui

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.text.TextLayoutResult
import androidx.test.core.app.ApplicationProvider
import io.github.uveshm003.unreel.R
import io.github.uveshm003.unreel.core.testing.ScreenshotSuite
import org.junit.Assert.assertFalse
import org.junit.Test
import org.robolectric.annotation.Config

class UnreelAppScreenshotTest : ScreenshotSuite("unreel_app") {
    @Composable
    override fun Screen() = UnreelApp()

    @Test
    @Config(qualifiers = "w480dp-h220dp-land-notnight-mdpi")
    fun shortWindow_largeFont_canReadFullDescriptionAndReturnToHeading() {
        setScreenshotContent(darkTheme = false, fontScale = 2f)
        val context = ApplicationProvider.getApplicationContext<Context>()
        val description = composeRule.onNodeWithText(context.getString(R.string.app_shell_placeholder))
        val layouts = mutableListOf<TextLayoutResult>()
        description.performSemanticsAction(SemanticsActions.GetTextLayoutResult) { it(layouts) }
        assertFalse("The description must lay out every line", layouts.single().didOverflowHeight)
        description.performScrollTo().assertIsDisplayed()
        captureCurrentScreen("short_window_large_font")
        composeRule.onNodeWithText(context.getString(R.string.app_name)).performScrollTo().assertIsDisplayed()
    }
}
