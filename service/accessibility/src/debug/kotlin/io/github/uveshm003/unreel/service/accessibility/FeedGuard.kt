package io.github.uveshm003.unreel.service.accessibility

import android.view.accessibility.AccessibilityNodeInfo
import io.github.uveshm003.unreel.core.data.PreviewCompatibility
import io.github.uveshm003.unreel.core.detection.DetectedSurface
import io.github.uveshm003.unreel.core.detection.DetectionEngine
import io.github.uveshm003.unreel.core.detection.DetectionEvent
import io.github.uveshm003.unreel.core.detection.DetectionInput
import io.github.uveshm003.unreel.core.model.ProtectedApp
import javax.inject.Inject

internal data class WindowEvent(val app: ProtectedApp, val window: Int, val kind: DetectionEvent, val generation: Long)

internal class FeedGuard @Inject constructor(private val device: AndroidPreviewDevice) {
    private val snapshots = WindowSnapshot()
    private val engine by lazy { DetectionEngine(listOfNotNull(device.rules)) }

    fun shouldBlock(event: WindowEvent, activeRoot: () -> AccessibilityNodeInfo?): Boolean {
        val version = (device.compatibility() as? PreviewCompatibility.Supported)?.versionCode ?: return false
        val tree = activeRoot()?.let { root ->
            try {
                if (root.windowId == event.window && root.packageName?.toString() == event.app.packageName) {
                    snapshots.read(root)
                } else {
                    null
                }
            } finally {
                root.recycle()
            }
        }
        val input = DetectionInput(event.kind, event.app.packageName, version, tree)
        return engine.detect(input).surface == DetectedSurface.ShortsPlayer
    }
}
