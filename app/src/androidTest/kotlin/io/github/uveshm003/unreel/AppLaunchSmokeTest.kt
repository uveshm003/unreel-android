package io.github.uveshm003.unreel

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** On-device smoke test: the real app (real Hilt graph) starts and renders. Run nightly in CI. */
@RunWith(AndroidJUnit4::class)
class AppLaunchSmokeTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun appLaunches_andRendersShell() {
        val tagline = composeRule.activity.getString(R.string.app_tagline)
        composeRule.onNodeWithText(tagline).assertIsDisplayed()
    }
}
