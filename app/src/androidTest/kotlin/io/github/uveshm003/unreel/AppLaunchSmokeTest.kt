package io.github.uveshm003.unreel

import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.github.uveshm003.unreel.feature.home.R.string as HomeStrings
import io.github.uveshm003.unreel.feature.onboarding.R.string as SetupStrings
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** On-device smoke test: the real app (real Hilt graph) starts and renders. Run nightly in CI. */
@RunWith(AndroidJUnit4::class)
class AppLaunchSmokeTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun launchesIntoSetupOrTheCompletedShell() {
        val resources = listOf(
            SetupStrings.setup_welcome,
            SetupStrings.setup_disclosure,
            SetupStrings.setup_service,
            SetupStrings.setup_guides,
            HomeStrings.home_description,
        )
        val labels = resources.map(composeRule.activity::getString)
        composeRule.waitUntil(timeoutMillis = 10_000) {
            labels.any { composeRule.onAllNodesWithText(it).fetchSemanticsNodes().isNotEmpty() }
        }
    }
}
