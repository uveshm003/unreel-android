package io.github.uveshm003.unreel.core.detection

import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import org.junit.Test

class DetectionEngineTest {
    private val bounds = NodeBounds(0, 0, 10, 10)
    private val player = NodeSnapshot("fixture.app:id/player", "Video", "Clip", true, true, false, bounds)
    private val root = group("Root", group("Panel", player))
    private val video = Matcher.ClassName("Video")
    private val packageName = "fixture.app"

    private fun group(name: String, vararg children: NodeSnapshot) =
        NodeSnapshot(null, name, null, true, false, false, bounds, children.toList())

    private fun rule(match: Matcher, id: String = "viewer", priority: Int = 2, range: VersionRange? = null) =
        DetectionRule(id, DetectedSurface.ReelsViewer, priority, match, range)

    private fun rules(vararg rules: DetectionRule) = DetectionRules(1, packageName, 1, rules.toList())

    private fun input(tree: NodeSnapshot? = root) = DetectionInput(DetectionEvent.ContentChanged, packageName, 42, tree)

    @Test
    fun matches_whole_nodes_and_strict_relationships() {
        val panel = Matcher.ClassName("Panel")
        val cases = listOf(
            Matcher.ViewId("fixture.app:id/player") to true,
            Matcher.ViewId("player") to true,
            Matcher.ViewId("layer") to false,
            Matcher.ViewId("another:id/player") to false,
            Matcher.ViewId("Player") to false,
            video to true,
            Matcher.ClassName("Vid") to false,
            Matcher.ContentDescription("Clip") to true,
            Matcher.ContentDescription("clip") to false,
            Matcher.Flags(visible = true, scrollable = true, selected = false) to true,
            Matcher.Flags(visible = true, scrollable = true, selected = true) to false,
            Matcher.All(listOf(panel, Matcher.HasChild(video))) to true,
            Matcher.All(listOf(Matcher.ClassName("Root"), Matcher.HasChild(video))) to false,
            Matcher.All(listOf(Matcher.ClassName("Root"), Matcher.HasDescendant(video))) to true,
            Matcher.All(listOf(video, Matcher.HasDescendant(video))) to false,
            Matcher.All(listOf(video, panel)) to false,
            Matcher.AnyOf(listOf(Matcher.ClassName("Absent"), video)) to true,
            Matcher.AnyOf(listOf(Matcher.ClassName("Absent"), Matcher.Flags(visible = false))) to false,
            Matcher.All(listOf(panel, Matcher.Not(Matcher.HasChild(video)))) to false,
            Matcher.All(listOf(video, Matcher.Not(panel))) to true,
        )
        for ((matcher, expected) in cases) {
            val result = DetectionEngine(listOf(rules(rule(matcher)))).detect(input())
            assertEquals(if (expected) "viewer" else null, result.ruleId, matcher.toString())
        }
    }

    @Test
    fun selects_by_package_inclusive_version_priority_then_file_order() {
        val file = rules(
            rule(video, "future", 10, VersionRange(min = 43)),
            rule(video, "old", 10, VersionRange(max = 41)),
            rule(video, "fallback", 0),
            rule(video, "first", 5, VersionRange(42, 42)),
            rule(video, "second", 5),
        )
        val engine = DetectionEngine(listOf(file))
        assertEquals(DetectionResult(DetectedSurface.ReelsViewer, false, "first"), engine.detect(input()))
        assertEquals("old", engine.detect(input().copy(versionCode = 41)).ruleId)
        assertEquals("future", engine.detect(input().copy(versionCode = 43)).ruleId)
        assertEquals(DetectionResult.None, engine.detect(input().copy(packageName = "other")))
        assertEquals(DetectionResult.None, engine.detect(input(null)))
        val limited = DetectionEngine(listOf(rules(rule(video, range = VersionRange(max = 41)))))
        assertEquals(DetectionResult.None, limited.detect(input()))
        assertFailsWith<IllegalArgumentException> { DetectionEngine(listOf(file, file)) }
    }

    @Test
    fun a_truncated_search_cannot_turn_into_a_positive_even_under_negation() {
        val absent = Matcher.ClassName("Missing")
        val engine =
            DetectionEngine(listOf(rules(rule(Matcher.Not(Matcher.HasDescendant(absent))), rule(video, "fallback"))))
        val wide = group("Panel", *Array(512) { player })
        val deep = (1..33).fold(player) { tree, _ -> group("Panel", tree) }
        assertEquals(DetectionResult.None, engine.detect(input(wide)))
        assertEquals(DetectionResult.None, engine.detect(input(deep)))
        assertEquals("viewer", engine.detect(input()).ruleId)
    }

    @Test
    fun matches_at_the_limits_and_stops_on_the_next_node() {
        val engine = DetectionEngine(listOf(rules(rule(video))))
        val atDepth = (1..32).fold(player) { tree, _ -> group("Panel", tree) }
        val siblings = Array(510) { group("Other") }
        assertEquals("viewer", engine.detect(input(atDepth)).ruleId)
        assertEquals(DetectionResult.None, engine.detect(input(group("Panel", atDepth))))
        assertEquals("viewer", engine.detect(input(group("Panel", *siblings, player))).ruleId)
        assertEquals(DetectionResult.None, engine.detect(input(group("Panel", *siblings, group("Other"), player))))
    }

    @Test
    fun budget_is_shared_across_rules_and_resets_for_each_detection() {
        val tree = group("Root", *Array(511) { player })
        val engine = DetectionEngine(listOf(rules(rule(Matcher.ClassName("Absent")), rule(video, "fallback", 1))))
        assertEquals(DetectionResult.None, engine.detect(input(tree)))
        assertEquals("fallback", engine.detect(input()).ruleId)
        val fast = DetectionEngine(listOf(rules(rule(Matcher.ClassName("Root")))))
        assertEquals("viewer", fast.detect(input(tree)).ruleId)
    }
}
