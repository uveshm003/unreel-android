package io.github.uveshm003.unreel.core.detection

import java.util.Collections
import kotlinx.serialization.Serializable

@Serializable
data class NodeBounds(val left: Int, val top: Int, val right: Int, val bottom: Int)

class NodeSnapshot(
    val viewIdResourceName: String?,
    val className: String?,
    val contentDescription: String? = null,
    val isVisibleToUser: Boolean,
    val isScrollable: Boolean,
    val isSelected: Boolean,
    val bounds: NodeBounds,
    children: List<NodeSnapshot> = emptyList(),
) {
    val children: List<NodeSnapshot> = if (children.isEmpty()) {
        emptyList()
    } else {
        Collections.unmodifiableList(
            ArrayList(children),
        )
    }
}

enum class DetectionEvent { WindowStateChanged, ContentChanged, ViewScrolled }

data class DetectionInput(
    val event: DetectionEvent,
    val packageName: String,
    val versionCode: Long,
    val root: NodeSnapshot?,
)

@Serializable
enum class DetectedSurface { None, ReelsTab, ReelsViewer, ShortsPlayer }

data class DetectionResult(val surface: DetectedSurface, val isSwipeToNext: Boolean, val ruleId: String?) {
    companion object {
        val None = DetectionResult(DetectedSurface.None, false, null)
    }
}
