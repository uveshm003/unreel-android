package io.github.uveshm003.unreel.core.detection

import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import org.junit.Test

class RulesParserTest {
    private val flag = """{"type":"flags","visible":false}"""

    private fun rule(match: String, extra: String = "") =
        """{"id":"sample","priority":7,"surface":"ReelsViewer","match":$match$extra}"""

    private fun file(vararg rules: String) =
        """{"schemaVersion":1,"package":"fixture.app","rulesVersion":2,"rules":[${rules.joinToString(",")}]}"""

    @Test
    fun reads_all_matchers_and_preserves_operands() {
        val cases = mapOf(
            """{"type":"viewId","value":"fixture"}""" to Matcher.ViewId("fixture"),
            """{"type":"className","value":"Player"}""" to Matcher.ClassName("Player"),
            """{"type":"contentDescription","value":"Clip"}""" to Matcher.ContentDescription("Clip"),
            """{"type":"flags","selected":true,"scrollable":false}""" to
                Matcher.Flags(scrollable = false, selected = true),
            """{"type":"hasChild","matcher":$flag}""" to Matcher.HasChild(Matcher.Flags(visible = false)),
            """{"type":"hasDescendant","matcher":$flag}""" to Matcher.HasDescendant(Matcher.Flags(visible = false)),
            """{"type":"not","matcher":$flag}""" to Matcher.Not(Matcher.Flags(visible = false)),
            """{"type":"all","matchers":[$flag]}""" to Matcher.All(listOf(Matcher.Flags(visible = false))),
            """{"type":"any","matchers":[$flag]}""" to Matcher.AnyOf(listOf(Matcher.Flags(visible = false))),
        )
        for ((json, expected) in cases) {
            val parsed = RulesParser.parse(file(rule(json)))
            assertEquals(
                DetectionRules(
                    1,
                    "fixture.app",
                    2,
                    listOf(DetectionRule("sample", DetectedSurface.ReelsViewer, 7, expected)),
                ),
                parsed,
            )
        }
    }

    @Test
    fun reports_invalid_files_and_nested_matchers() {
        val valid = file(rule(flag))
        val badFiles = listOf(
            "[]" to "object",
            """{"schemaVersion":2}""" to "schemaVersion",
            valid.replace("Version\":1", "Version\":\"1\"") to "schemaVersion",
            valid.replace("fixture.app", " ") to "package",
            valid.replace("rulesVersion\":2", "rulesVersion\":0") to "rulesVersion",
            file(rule(flag), rule(flag)) to "Duplicate rule id: sample",
            valid.replace("sample", "") to "Rule id",
            valid.replace("ReelsViewer", "None") to "None",
            file(rule(flag, """, "versionCode":{"min":4,"max":3}""")) to "versionCode",
            file(rule(flag, """, "versionCode":{"min":-1}""")) to "versionCode",
            file(rule("""{"type":"madeUp"}""")) to "madeUp",
            file(rule("""{"type":"flags","typo":true}""")) to "typo",
            file(rule("""{"type":"hasChild","matcher":{"type":"flags"}}""")) to "flags",
            file(rule("""{"type":"all","matchers":[]}""")) to "matchers",
            file(rule("""{"type":"any","matchers":[]}""")) to "matchers",
        ) + listOf("viewId", "className", "contentDescription").map {
            file(rule("""{"type":"$it","value":" "}""")) to it
        }
        for ((json, message) in badFiles) {
            val error = assertFailsWith<IllegalArgumentException>(json) { RulesParser.parse(json) }
            assertTrue(error.message.orEmpty().contains(message), "$json -> ${error.message}")
        }
    }

    @Test
    fun version_ranges_are_inclusive_and_allow_open_endpoints() {
        val range = RulesParser.parse(
            file(rule(flag, """, "versionCode":{"min":3,"max":5}""")),
        ).rules.single().versionCode!!
        assertEquals(VersionRange(3, 5), range)
        assertTrue(3L in range && 5L in range)
        assertFalse(2L in range || 6L in range)
        assertTrue(Long.MAX_VALUE in VersionRange(min = 7))
        assertTrue(0L in VersionRange(max = 0))
    }

    @Test
    fun snapshot_owns_its_children_and_input_can_represent_a_missing_root() {
        val bounds = NodeBounds(0, 0, 10, 10)
        val leaf = NodeSnapshot(null, null, null, false, false, false, bounds)
        val children = mutableListOf(leaf)
        val root = NodeSnapshot("id", "class", "desc", true, true, true, bounds, children)
        children.clear()
        assertEquals(listOf(leaf), root.children)
        assertFailsWith<UnsupportedOperationException> { (root.children as MutableList).clear() }
        assertEquals(
            listOf("id", "class", "desc"),
            listOf(root.viewIdResourceName, root.className, root.contentDescription),
        )
        assertTrue(root.isVisibleToUser && root.isScrollable && root.isSelected)
        assertEquals(bounds, root.bounds)
        val input = DetectionInput(DetectionEvent.ContentChanged, "fixture.app", 42, root)
        assertEquals(root, input.root)
        assertEquals(null, input.copy(root = null).root)
        assertEquals(DetectionResult(DetectedSurface.None, false, null), DetectionResult.None)
    }
}
