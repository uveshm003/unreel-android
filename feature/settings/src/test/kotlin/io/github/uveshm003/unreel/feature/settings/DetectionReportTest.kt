package io.github.uveshm003.unreel.feature.settings

import android.content.Context
import android.content.pm.PackageInfo
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
class DetectionReportTest {
    @Test
    fun prefilledValuesRoundTripAndMatchTheCheckedInForm() {
        val version = "42.0+beta & #1"
        val device = "A/B #5, Android 17"
        val uri = DetectionReport(ReportApp.YouTube, version, "v1 + dev", device).issueUrl()
        assertEquals("https", uri.scheme)
        assertEquals("github.com", uri.host)
        assertEquals("/uveshm003/unreel-android/issues/new", uri.path)
        assertEquals("detection.yml", uri.getQueryParameter("template"))
        assertEquals("YouTube", uri.getQueryParameter("app"))
        assertEquals(version, uri.getQueryParameter("app_version"))
        assertEquals("v1 + dev", uri.getQueryParameter("unreel_version"))
        assertEquals(device, uri.getQueryParameter("device"))
        assertEquals(setOf("template", "app", "app_version", "unreel_version", "device"), uri.queryParameterNames)
        val form = checkNotNull(javaClass.getResource("/detection.yml")).readText()
        val inputFields = Regex("""(?m)^  - type: input\s+id: (\w+)\s*$""").findAll(form)
            .map { it.groupValues[1] }.toSet()
        assertTrue(inputFields.containsAll(uri.queryParameterNames - "template"))
    }

    @Test
    fun missingVersionIsLeftForTheReporterToFillIn() {
        val uri = DetectionReport(ReportApp.Other, null, "1.2", "Device").issueUrl()
        assertEquals("Other", uri.getQueryParameter("app"))
        assertNull(uri.getQueryParameter("app_version"))
    }

    @Test
    @Config(sdk = [30, 35])
    fun readsOnlyTheSelectedInstalledVersion() {
        val packages = ApplicationProvider.getApplicationContext<Context>().packageManager
        shadowOf(packages).installPackage(
            PackageInfo().apply {
                packageName = "com.google.android.youtube"
                versionName = "20.4"
            },
        )
        assertEquals("20.4", ReportApp.YouTube.installedVersion(packages))
        assertNull(ReportApp.Instagram.installedVersion(packages))
        assertNull(ReportApp.Other.installedVersion(packages))
    }
}
