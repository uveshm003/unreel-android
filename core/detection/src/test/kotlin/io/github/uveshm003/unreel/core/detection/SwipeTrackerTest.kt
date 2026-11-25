package io.github.uveshm003.unreel.core.detection

import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import org.junit.Test

class SwipeTrackerTest {
    private val bounds = NodeBounds(0, 0, 40, 40)
    private val viewer = node("Viewer")
    private val pager = node("Pager")
    private val scrolled = DetectionEvent.ViewScrolled
    private val entered = DetectionEvent.WindowStateChanged
    private val changed = DetectionEvent.ContentChanged
    private val data = """{
        "schemaVersion":1,"package":"fixture.app","rulesVersion":1,"rules":[
          {"id":"viewer","surface":"ReelsViewer","priority":1,"match":{"type":"className","value":"Viewer"},
           "swipeSignal":{"source":{"type":"className","value":"Pager"}}},
          {"id":"tab","surface":"ReelsTab","priority":0,"match":{"type":"className","value":"Tab"}}
        ]}"""
    private val engine = DetectionEngine(listOf(RulesParser.parse(data)))

    private fun node(name: String) = NodeSnapshot(null, name, null, true, false, false, bounds)

    private fun event(kind: DetectionEvent, time: Long?, index: Int?, root: NodeSnapshot = viewer) =
        DetectionInput(kind, "fixture.app", 10, root, time, index?.let { ScrollPosition(pager, it) })

    private fun checkSequence(vararg steps: Pair<DetectionInput, Boolean>) {
        for ((event, expected) in steps) {
            assertEquals(expected, engine.detect(event).isSwipeToNext, "${event.event} at ${event.eventTimeMillis}")
        }
    }

    @Test
    fun tab_to_viewer_then_two_distinct_forward_swipes() = checkSequence(
        event(entered, 1, null, node("Tab")) to false,
        event(entered, 2, 2) to false,
        event(scrolled, 3, 3) to true,
        event(scrolled, 3, 3) to false,
        event(scrolled, 4, 3) to false,
        event(scrolled, 5, 4) to true,
    )

    @Test
    fun single_reel_from_messages_stays_uninterrupted_until_the_pager_advances() = checkSequence(
        event(entered, 1, null, node("Messages")) to false,
        event(changed, 2, null) to false,
        event(scrolled, 3, 8) to false,
        event(changed, 4, 9) to false,
        event(scrolled, 5, 9).copy(scrollPosition = ScrollPosition(node("Comments"), 9)) to false,
        event(scrolled, 6, 9) to true,
        event(scrolled, 7, 8) to false,
        event(scrolled, 8, 9) to true,
    )

    @Test
    fun exits_windows_packages_and_stale_events_do_not_create_swipes() = checkSequence(
        event(scrolled, 10, 1) to false,
        event(scrolled, 11, 2) to true,
        event(entered, 12, 9) to false,
        event(scrolled, 11, 1) to false,
        event(scrolled, 13, 9) to false,
        event(changed, 14, null).copy(packageName = "other") to false,
        event(scrolled, 15, 15) to false,
        event(scrolled, 16, 16) to true,
        event(changed, 17, null, node("Messages")) to false,
        event(scrolled, 18, 32) to false,
        event(scrolled, 19, 33).copy(versionCode = 11) to false,
    )

    @Test
    fun missing_time_and_indices_require_a_new_baseline() = checkSequence(
        event(entered, 1, null) to false,
        event(scrolled, 2, -1) to false,
        event(scrolled, 3, 1) to false,
        event(scrolled, 4, 2) to true,
        event(scrolled, null, 3) to false,
        event(scrolled, 5, 4) to false,
        event(scrolled, 6, 5) to true,
    )

    @Test
    fun an_unrelated_source_with_a_pager_child_and_an_exhausted_budget_cannot_signal_a_swipe() {
        val wrapper = NodeSnapshot(null, "Other", null, true, false, false, bounds, listOf(pager))
        val tree = NodeSnapshot(null, "Root", null, true, false, false, bounds, List(510) { pager } + viewer)
        checkSequence(
            event(entered, 1, 1) to false,
            event(scrolled, 2, 2).copy(scrollPosition = ScrollPosition(wrapper, 2)) to false,
            event(scrolled, 3, 2, tree) to false,
            event(scrolled, 4, 2) to true,
        )
    }

    @Test
    fun swipe_signal_requires_a_viewer_and_a_valid_source_matcher() {
        assertFailsWith<IllegalArgumentException> { RulesParser.parse(data.replace("ReelsViewer", "ReelsTab")) }
        assertFailsWith<IllegalArgumentException> { RulesParser.parse(data.replace("\"Pager\"", "\" \"")) }
        val shorts = DetectionEngine(listOf(RulesParser.parse(data.replace("ReelsViewer", "ShortsPlayer"))))
        assertEquals(DetectedSurface.ShortsPlayer, shorts.detect(event(entered, 1, 0)).surface)
        assertEquals(true, shorts.detect(event(scrolled, 2, 1)).isSwipeToNext)
    }
}
