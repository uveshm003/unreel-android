package io.github.uveshm003.unreel.feature.apps

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import io.github.uveshm003.unreel.core.data.PreviewCompatibility
import io.github.uveshm003.unreel.core.testing.ScreenshotSuite
import org.junit.Test
import org.robolectric.annotation.Config

class FeedPreviewUiTest : ScreenshotSuite("shorts_preview") {
    private var deviceStatus by mutableStateOf<PreviewStatus?>(PreviewStatus(PreviewCompatibility.Supported(1), false))
    private var consent by mutableStateOf(false)

    @Composable
    override fun Screen() {
        PreviewPanel(consent, connected = false, error = false, status = deviceStatus) { consent = it }
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

    @Test
    @Config(qualifiers = PHONE_LIGHT)
    fun unsupportedInstallShowsItsBuildAndStillAllowsRevocation() {
        consent = true
        deviceStatus = PreviewStatus(PreviewCompatibility.Unsupported("99.0", 1), true)
        setScreenshotContent(darkTheme = false)
        composeRule.onNodeWithText("YouTube 99.0 (build 1)", substring = true).assertExists()
        composeRule.onNodeWithText("Open accessibility settings").assertDoesNotExist()
        composeRule.onNodeWithText("Turn off Shorts protection").performClick()
        composeRule.onNodeWithText("Set up Shorts protection").assertDoesNotExist()
    }

    @Test
    @Config(qualifiers = PHONE_LIGHT)
    fun enabledServiceCanWaitForConnectionWithoutRepeatingSetup() {
        consent = true
        deviceStatus = PreviewStatus(PreviewCompatibility.Supported(1), true)
        setScreenshotContent(darkTheme = false)
        composeRule.onNodeWithText("Waiting for the service to connect", substring = true).assertExists()
        composeRule.onNodeWithText("Open accessibility settings").assertDoesNotExist()
    }
}
