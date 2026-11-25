package io.github.uveshm003.unreel.feature.settings

import android.content.ActivityNotFoundException
import android.content.ClipboardManager
import android.content.ContextWrapper
import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.core.view.drawToBitmap
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.takahirom.roborazzi.captureRoboImage
import io.github.uveshm003.unreel.core.designsystem.theme.UnreelTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowBuild
import org.robolectric.shadows.ShadowDialog

@RunWith(AndroidJUnit4::class)
class DetectionReportDialogTest {
    @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()

    @Test
    @Config(qualifiers = "w411dp-h891dp-notnight-xxhdpi")
    fun previewRequiresASelectionAndOpensTheFormOnlyOnConfirmation() {
        ShadowBuild.setManufacturer("Example")
        ShadowBuild.setModel("Phone")
        compose.setContent { UnreelTheme(dynamicColor = false) { DetectionReportDialog("1.2.3") {} } }
        val open = compose.onNodeWithText("Open GitHub")
        open.assertIsNotEnabled()
        compose.onNodeWithText("Other app").performClick()
        compose.waitUntil(timeoutMillis = 10_000) { runCatching { open.assertIsEnabled() }.isSuccess }
        val activity = compose.activity
        assertEquals(null, shadowOf(activity).peekNextStartedActivity())
        compose.runOnIdle {
            checkNotNull(ShadowDialog.getLatestDialog().window).decorView.drawToBitmap()
        }.captureRoboImage("src/test/screenshots/report_preview_phone_light.png")
        open.performClick()
        val intent = shadowOf(activity).nextStartedActivity
        assertEquals(Intent.ACTION_VIEW, intent.action)
        assertEquals("Other", intent.data?.getQueryParameter("app"))
    }

    @Test
    fun missingBrowserOffersTheSameLinkOnTheClipboard() {
        val activity = compose.activity
        var attempted: Intent? = null
        val noBrowser = object : ContextWrapper(activity) {
            override fun startActivity(intent: Intent) {
                attempted = intent
                throw ActivityNotFoundException()
            }
        }
        compose.setContent {
            CompositionLocalProvider(LocalContext provides noBrowser) { DetectionReportDialog("1.2.3") {} }
        }
        compose.onNodeWithText("Instagram").performClick()
        val open = compose.onNodeWithText("Open GitHub")
        compose.waitUntil(timeoutMillis = 10_000) { runCatching { open.assertIsEnabled() }.isSuccess }
        open.performClick()
        compose.onNodeWithText("Copy report link").performClick()
        val clip = activity.getSystemService(ClipboardManager::class.java).primaryClip
        assertEquals(checkNotNull(attempted).dataString, checkNotNull(clip).getItemAt(0).text.toString())
    }
}
