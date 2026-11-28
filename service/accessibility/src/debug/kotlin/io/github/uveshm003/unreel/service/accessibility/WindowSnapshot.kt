package io.github.uveshm003.unreel.service.accessibility

import android.graphics.Rect
import android.view.accessibility.AccessibilityNodeInfo
import io.github.uveshm003.unreel.core.detection.NodeBounds
import io.github.uveshm003.unreel.core.detection.NodeSnapshot

/** Drops incomplete windows. Only structural fields used by the shipped rule are read. */
internal class WindowSnapshot(private val limit: Int = 512, private val depthLimit: Int = 40) {
    fun read(root: AccessibilityNodeInfo): NodeSnapshot? = Capture(root.packageName?.toString()).node(root, 0)

    private inner class Capture(private val packageName: String?) {
        private var remaining = limit
        private val rect = Rect()

        fun node(info: AccessibilityNodeInfo, depth: Int): NodeSnapshot? {
            val complete = remaining-- > 0 && depth <= depthLimit && info.packageName?.toString() == packageName
            val children = (if (complete) children(info, depth) else null) ?: return null
            info.getBoundsInScreen(rect)
            return NodeSnapshot(
                info.viewIdResourceName,
                info.className?.toString(),
                null,
                info.isVisibleToUser,
                info.isScrollable,
                info.isSelected,
                NodeBounds(rect.left, rect.top, rect.right, rect.bottom),
                children,
            )
        }

        private fun children(info: AccessibilityNodeInfo, depth: Int): List<NodeSnapshot>? {
            val result = ArrayList<NodeSnapshot>()
            for (index in 0 until info.childCount) {
                val snapshot = info.getChild(index)?.let { child ->
                    try {
                        node(child, depth + 1)
                    } finally {
                        child.recycle()
                    }
                }
                result.add(snapshot ?: return null)
            }
            return result
        }
    }
}
