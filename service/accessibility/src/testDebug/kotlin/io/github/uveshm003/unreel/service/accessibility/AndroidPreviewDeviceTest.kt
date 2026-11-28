package io.github.uveshm003.unreel.service.accessibility

import android.accessibilityservice.AccessibilityServiceInfo
import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageInfo
import android.view.accessibility.AccessibilityManager
import androidx.test.core.app.ApplicationProvider
import io.github.uveshm003.unreel.core.data.PreviewCompatibility
import io.github.uveshm003.unreel.core.model.ProtectedApp
import kotlin.test.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf

@RunWith(RobolectricTestRunner::class)
class AndroidPreviewDeviceTest {
    private val context = ApplicationProvider.getApplicationContext<Context>()
    private val device = AndroidPreviewDevice(context)

    @Test
    fun installingAndUpdatingYouTubeIsComparedWithTheShippedRule() {
        val verified = checkNotNull(device.rules?.rules?.single()?.versionCode?.min)
        assertEquals(PreviewCompatibility.Missing, device.compatibility())
        install(verified)
        assertEquals(PreviewCompatibility.Supported(verified), device.compatibility())
        install(verified + 1)
        assertEquals(PreviewCompatibility.Unsupported("fixture", verified + 1), device.compatibility())
    }

    @Test
    fun onlyTheUnreelServiceCountsAsEnabled() {
        val manager = checkNotNull(context.getSystemService(AccessibilityManager::class.java))
        val shadow = shadowOf(manager)
        val own = ComponentName(context, UnreelAccessibilityService::class.java)
        fun service(component: ComponentName) = object : AccessibilityServiceInfo() {
            override fun getId(): String = component.flattenToShortString()
        }
        shadow.setEnabledAccessibilityServiceList(listOf(service(ComponentName(context, "other.Service"))))
        assertEquals(false, device.serviceEnabled())
        shadow.setEnabledAccessibilityServiceList(listOf(service(own)))
        assertEquals(true, device.serviceEnabled())
        shadow.setEnabledAccessibilityServiceList(emptyList())
        assertEquals(false, device.serviceEnabled())
    }

    private fun install(code: Long) {
        shadowOf(context.packageManager).installPackage(
            PackageInfo().apply {
                packageName = ProtectedApp.YouTube.packageName
                versionName = "fixture"
                longVersionCode = code
            },
        )
    }
}
