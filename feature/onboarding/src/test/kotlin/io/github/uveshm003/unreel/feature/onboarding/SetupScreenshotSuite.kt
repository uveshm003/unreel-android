package io.github.uveshm003.unreel.feature.onboarding

import android.content.Intent
import android.os.PowerManager
import android.provider.Settings
import androidx.compose.runtime.Composable
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.lifecycle.Lifecycle
import io.github.uveshm003.unreel.core.model.SetupStep
import io.github.uveshm003.unreel.core.testing.ScreenshotSuite
import org.junit.Assert.assertEquals
import org.junit.Test
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowBuild

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

class GuidesScreenshotTest : SetupScreenshotSuite(SetupStep.Guides) {
    @Test
    @Config(qualifiers = PHONE_LIGHT)
    fun xiaomiGuideOpensManufacturerPage() {
        ShadowBuild.setManufacturer("Xiaomi")
        capture("xiaomi_phone_light", darkTheme = false)
        click(R.string.battery_learn_more)
        val intent = shadowOf(composeRule.activity).nextStartedActivity
        assertEquals(Intent.ACTION_VIEW, intent.action)
        assertEquals("https://dontkillmyapp.com/xiaomi", intent.dataString)
    }

    @Test
    fun systemSettingsRoundTripRefreshesExemption() {
        setScreenshotContent(darkTheme = false)
        val activity = composeRule.activity
        composeRule.onNodeWithText(activity.getString(R.string.battery_optimised)).assertExists()
        click(R.string.battery_open_settings)
        assertEquals(
            Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS,
            shadowOf(activity).nextStartedActivity.action,
        )
        val scenario = composeRule.activityRule.scenario
        scenario.moveToState(Lifecycle.State.CREATED)
        shadowOf(checkNotNull(activity.getSystemService(PowerManager::class.java)))
            .setIgnoringBatteryOptimizations(activity.packageName, true)
        scenario.moveToState(Lifecycle.State.RESUMED)
        composeRule.onNodeWithText(activity.getString(R.string.battery_exempt)).assertExists()
    }

    private fun click(label: Int) {
        composeRule.onNodeWithText(composeRule.activity.getString(label)).performScrollTo().performClick()
    }
}
