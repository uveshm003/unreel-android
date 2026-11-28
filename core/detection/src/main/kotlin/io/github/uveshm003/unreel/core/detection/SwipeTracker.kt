package io.github.uveshm003.unreel.core.detection

/** A tracker is idle outside a timed viewer. An active viewer may still be waiting for its first index. */
internal class SwipeTracker {
    private var lastEventTime = Long.MIN_VALUE
    private var active: Viewer? = null

    fun observe(input: DetectionInput, rule: DetectionRule?, index: Int?): Boolean {
        val time = input.eventTimeMillis
        if (time != null && time <= lastEventTime) return false
        if (time != null) lastEventTime = time
        val current = active
        return when {
            time == null || rule?.swipeSignal == null -> {
                active = null
                false
            }

            current == null || !current.contains(input, rule.id) -> {
                active = Viewer(input.packageName, input.versionCode, rule.id, index)
                false
            }

            else -> current.observe(input.event, index)
        }
    }

    private class Viewer(val packageName: String, val versionCode: Long, val ruleId: String, var itemIndex: Int?) {
        fun contains(input: DetectionInput, id: String) = input.event != DetectionEvent.WindowStateChanged &&
            input.packageName == packageName && input.versionCode == versionCode && id == ruleId

        fun observe(event: DetectionEvent, index: Int?): Boolean {
            val previous = itemIndex
            if (index == null || (previous != null && event != DetectionEvent.ViewScrolled)) return false
            itemIndex = index
            return previous != null && index > previous
        }
    }
}
