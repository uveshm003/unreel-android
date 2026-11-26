package io.github.uveshm003.unreel.service.accessibility

import android.content.Context
import android.content.pm.PackageManager
import android.view.accessibility.AccessibilityNodeInfo
import dagger.hilt.android.qualifiers.ApplicationContext
import io.github.uveshm003.unreel.core.detection.DetectedSurface
import io.github.uveshm003.unreel.core.detection.DetectionEngine
import io.github.uveshm003.unreel.core.detection.DetectionEvent
import io.github.uveshm003.unreel.core.detection.DetectionInput
import io.github.uveshm003.unreel.core.detection.RulesParser
import io.github.uveshm003.unreel.core.model.ProtectedApp
import java.io.IOException
import javax.inject.Inject

internal data class WindowEvent(val app: ProtectedApp, val window: Int, val kind: DetectionEvent, val generation: Long)

internal class FeedGuard @Inject constructor(@ApplicationContext private val context: Context) {
    private val snapshots = WindowSnapshot()
    private val file by lazy {
        val pkg = ProtectedApp.YouTube.packageName
        try {
            context.assets.open("$pkg.json").bufferedReader().use { RulesParser.parse(it.readText()) }.takeIf {
                it.packageName ==
                    pkg
            }
        } catch (_: IOException) {
            null
        } catch (_: IllegalArgumentException) {
            null
        }
    }
    private val engine by lazy { DetectionEngine(listOfNotNull(file)) }

    fun shouldBlock(event: WindowEvent, activeRoot: () -> AccessibilityNodeInfo?): Boolean {
        val version = supportedVersion(event.app.packageName) ?: return false
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

    private fun supportedVersion(pkg: String): Long? {
        val rules = file?.takeIf { it.packageName == pkg } ?: return null
        return try {
            val version = context.packageManager.getPackageInfo(pkg, 0).longVersionCode
            version.takeIf { rules.rules.any { rule -> rule.versionCode?.contains(version) == true } }
        } catch (_: PackageManager.NameNotFoundException) {
            null
        }
    }
}
