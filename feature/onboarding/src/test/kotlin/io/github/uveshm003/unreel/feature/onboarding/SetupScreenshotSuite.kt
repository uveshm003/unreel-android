package io.github.uveshm003.unreel.feature.onboarding

import androidx.compose.runtime.Composable
import io.github.uveshm003.unreel.core.model.SetupStep
import io.github.uveshm003.unreel.core.testing.ScreenshotSuite
import org.junit.Test
import org.robolectric.annotation.Config

abstract class SetupScreenshotSuite(private val step: SetupStep) : ScreenshotSuite("setup_${step.key}") {
    @Composable
    override fun Screen() = SetupScreen(step, advance = {}, back = {}, skipTips = {})

    @Test
    @Config(qualifiers = PHONE_LIGHT)
    fun largeFont() = capture("phone_light_large_font", darkTheme = false, fontScale = 2f)
}

class WelcomeScreenshotTest : SetupScreenshotSuite(SetupStep.Welcome)

class DisclosureScreenshotTest : SetupScreenshotSuite(SetupStep.Disclosure)

class ServiceScreenshotTest : SetupScreenshotSuite(SetupStep.Service)

class GuidesScreenshotTest : SetupScreenshotSuite(SetupStep.Guides)
