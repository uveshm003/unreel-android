package io.github.uveshm003.unreel.feature.apps

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import io.github.uveshm003.unreel.core.testing.ScreenshotSuite
import org.junit.Test
import org.robolectric.annotation.Config

class FeedPreviewUiTest : ScreenshotSuite("shorts_preview") {
    @Composable
    override fun Screen() {
        var enabled by remember { mutableStateOf(false) }
        PreviewPanel(enabled, connected = false, error = false) { enabled = it }
    }

    @Test
    @Config(qualifiers = PHONE_LIGHT)
    fun settingsRequireConsentAndRevokingRemovesTheShortcut() {
        setScreenshotContent(darkTheme = false, fontScale = 2f)
        composeRule.onNodeWithText("Set up Shorts protection").performClick()
        composeRule.onNodeWithText("Not now").performClick()
        composeRule.onNodeWithText("Open accessibility settings").assertDoesNotExist()
        composeRule.onNodeWithText("Set up Shorts protection").performClick()
        composeRule.onNodeWithText("I agree").performClick()
        composeRule.onNodeWithText("Open accessibility settings").assertExists()
        composeRule.onNodeWithText("Turn off Shorts protection").performClick()
        composeRule.onNodeWithText("Open accessibility settings").assertDoesNotExist()
    }
}
