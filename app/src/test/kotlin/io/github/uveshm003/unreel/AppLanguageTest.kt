package io.github.uveshm003.unreel

import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isDialog
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.core.os.LocaleListCompat
import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.HiltTestApplication
import io.github.uveshm003.unreel.feature.onboarding.R.string as SetupStrings
import io.github.uveshm003.unreel.feature.settings.R.string as SettingsStrings
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
@Config(application = HiltTestApplication::class)
class AppLanguageTest {
    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    @Config(sdk = [32], qualifiers = "en")
    fun inAppLanguageSwitchRecreatesSettingsOnAndroid12AndCanFollowSystemAgain() {
        try {
            see(SetupStrings.setup_next).performClick()
            see(SetupStrings.setup_disclosure)
            see(SetupStrings.setup_next).performClick()
            see(SetupStrings.setup_skip_tips).performClick()
            see(SettingsStrings.settings_label).performClick()
            see(SettingsStrings.settings_language).performClick()
            see(SettingsStrings.settings_hindi).performClick()
            composeRule.waitUntil(timeoutMillis = 10_000) {
                composeRule.onAllNodesWithText("भाषा").fetchSemanticsNodes().isNotEmpty()
            }
            composeRule.onNodeWithText("भाषा").performClick()
            composeRule.onNode(hasText("English") and hasAnyAncestor(isDialog())).performClick()
            composeRule.waitUntil(timeoutMillis = 10_000) {
                composeRule.onAllNodesWithText("Language").fetchSemanticsNodes().isNotEmpty()
            }
            composeRule.onNodeWithText("Language").performClick()
            composeRule.onNode(hasText("System default") and hasAnyAncestor(isDialog())).performClick()
            composeRule.runOnIdle { assertEquals("", AppCompatDelegate.getApplicationLocales().toLanguageTags()) }
        } finally {
            composeRule.runOnUiThread { AppCompatDelegate.setApplicationLocales(LocaleListCompat.getEmptyLocaleList()) }
        }
    }

    private fun see(resource: Int): SemanticsNodeInteraction {
        val text = composeRule.activity.getString(resource)
        composeRule.waitUntil(timeoutMillis = 10_000) {
            composeRule.onAllNodesWithText(text).fetchSemanticsNodes().isNotEmpty()
        }
        return composeRule.onNodeWithText(text)
    }
}
