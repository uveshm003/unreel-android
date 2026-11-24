package io.github.uveshm003.unreel.ui

import androidx.activity.ComponentActivity
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.github.uveshm003.unreel.core.designsystem.theme.UnreelTheme
import io.github.uveshm003.unreel.feature.apps.AppsRoute
import io.github.uveshm003.unreel.feature.home.HomeRoute
import io.github.uveshm003.unreel.feature.insights.InsightsRoute
import io.github.uveshm003.unreel.feature.schedule.ScheduleRoute
import io.github.uveshm003.unreel.feature.settings.SettingsRoute
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ShellNavigationTest {
    @get:Rule val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun allTabsOpenAndBackReturnsDirectlyHome() {
        composeRule.setContent { UnreelTheme { UnreelApp() } }
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
