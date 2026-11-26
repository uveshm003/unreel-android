package io.github.uveshm003.unreel.ui

import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.HiltTestApplication
import io.github.uveshm003.unreel.MainActivity
import io.github.uveshm003.unreel.core.data.OnboardingRepository
import io.github.uveshm003.unreel.core.model.SetupStep
import io.github.uveshm003.unreel.feature.apps.AppsRoute
import io.github.uveshm003.unreel.feature.home.HomeRoute
import io.github.uveshm003.unreel.feature.insights.InsightsRoute
import io.github.uveshm003.unreel.feature.schedule.ScheduleRoute
import io.github.uveshm003.unreel.feature.settings.SettingsRoute
import javax.inject.Inject
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@HiltAndroidTest
@Config(application = HiltTestApplication::class)
@RunWith(AndroidJUnit4::class)
class ShellNavigationTest {
    @get:Rule(order = 0)
    val hilt = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Inject lateinit var onboarding: OnboardingRepository

    @Before
    fun finishSetup() {
        hilt.inject()
        runBlocking {
            onboarding.advance(SetupStep.Welcome)
            onboarding.advance(SetupStep.Disclosure)
            onboarding.skipTips()
        }
        val home = composeRule.activity.getString(HomeRoute.label)
        composeRule.waitUntil(timeoutMillis = 10_000) {
            composeRule.onAllNodesWithText(home).fetchSemanticsNodes().isNotEmpty()
        }
    }

    @Test
    fun allTabsOpenAndBackReturnsDirectlyHome() {
        val tabs = listOf(AppsRoute, ScheduleRoute, InsightsRoute, SettingsRoute, HomeRoute)
        for (route in tabs) {
            val label = composeRule.activity.getString(route.label)
            val tab = composeRule.onNode(hasText(label) and hasClickAction())
            tab.performClick().assertIsSelected()
            composeRule.onNode(hasText(label) and SemanticsMatcher.keyIsDefined(SemanticsProperties.Heading))
                .assertIsDisplayed()
        }
        composeRule.onNode(
            hasText(composeRule.activity.getString(InsightsRoute.label)) and hasClickAction(),
        ).performClick()
        composeRule.runOnIdle { composeRule.activity.onBackPressedDispatcher.onBackPressed() }
        composeRule.onNode(
            hasText(composeRule.activity.getString(HomeRoute.label)) and hasClickAction(),
        ).assertIsSelected()
    }
}
