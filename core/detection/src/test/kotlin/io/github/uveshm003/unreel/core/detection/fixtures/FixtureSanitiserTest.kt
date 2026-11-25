package io.github.uveshm003.unreel.core.detection.fixtures

import java.io.File
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class FixtureSanitiserTest {
    @get:Rule val temp = TemporaryFolder()

    private val labels = setOf("Library")
    private val personal =
        listOf("@fictional_handle", "caption example", "someone@example.test", "555-0100", "comment example")
    private val xml = """<hierarchy><node class="fixture.Player" resource-id="fixture.app:id/player"
        content-desc="Library" text="caption example" bounds="[-2,1][13,40]" scrollable="true" selected="false">
        <node class="fixture.Child" content-desc="@fictional_handle" text="someone@example.test"
        custom="555-0100" bounds="[0,2][6,7]" visible-to-user="false" selected="true">comment example</node>
        </node></hierarchy>"""
    private val json = """{"text":"caption example","viewIdResourceName":"fixture.app:id/player","className":"fixture.Player",
        "contentDescription":"Library","isVisibleToUser":true,"isScrollable":true,"isSelected":false,
        "bounds":{"left":-2,"top":1,"right":13,"bottom":40,"personal":"555-0100"},"children":[{
        "className":"fixture.Child","viewIdResourceName":null,"contentDescription":"@fictional_handle",
        "text":"someone@example.test","extra":{"contentDescription":"comment example"},"isSelected":true,
        "bounds":{"left":0,"top":2,"right":6,"bottom":7}}]}"""

    @Test
    fun xml_and_json_produce_the_same_sanitised_tree() {
        val sanitiser = FixtureSanitiser(labels)
        val fixture = sanitiser.sanitise(xml, xml = true)
        assertEquals(fixture, sanitiser.sanitise(json, xml = false))
        assertEquals(fixture, sanitiser.sanitise(fixture, xml = false))
        personal.forEach { assertFalse(it in fixture) }
        val root = Json.parseToJsonElement(fixture).jsonObject
        val child = root.getValue("children").jsonArray.single().jsonObject
        assertEquals("Library", root.getValue("contentDescription").jsonPrimitive.content)
        assertEquals("[redacted]", child.getValue("contentDescription").jsonPrimitive.content)
        assertEquals("true", root.getValue("isScrollable").toString())
        assertEquals("true", child.getValue("isSelected").toString())
        assertEquals("false", child.getValue("isVisibleToUser").toString())
        assertEquals("-2", root.getValue("bounds").jsonObject.getValue("left").toString())
        assertTrue("fixture.app:id/player" in fixture && "fixture.Player" in fixture)
    }

    @Test
    fun unknown_descriptions_are_redacted_and_malformed_identifiers_are_dropped() {
        val dirty = json.replace("fixture.Player", "@fictional_handle").replace("fixture.app:id/player", "555-0100")
        val fixture = FixtureSanitiser(emptySet()).sanitise(dirty, xml = false)
        assertFalse("Library" in fixture)
        personal.forEach { assertFalse(it in fixture) }
        val almost = FixtureSanitiser(labels).sanitise(xml.replace("Library", "Library caption example"), xml = true)
        assertFalse("caption example" in almost)
    }

    @Test
    fun cli_applies_only_the_requested_packages_allowlist() {
        val labelsDir = temp.newFolder("labels")
        File(labelsDir, "fixture.app.json").writeText("""["Library"]""")
        val input = temp.newFile("input.json").apply { writeText(json) }
        val output = File(temp.root, "out.json")
        convertFixture(arrayOf("fixture.app", input.path, output.path), labelsDir)
        assertTrue("Library" in output.readText())
        convertFixture(arrayOf("other.app", input.path, output.path), labelsDir)
        assertFalse("Library" in output.readText())
    }

    @Test
    fun cli_writes_only_after_conversion_and_keeps_the_input() {
        val source = temp.newFile("dump.xml").apply { writeText(xml) }
        val out = File(temp.root, "fixture.json")
        val args = arrayOf("fixture.app", source.path, out.path)
        main(args)
        val fixture = out.readText()
        assertFalse("Library" in fixture)
        assertEquals(xml, source.readText())
        source.writeText("<hierarchy>someone@example.test</hierarchy>")
        val failure = assertFailsWith<IllegalArgumentException> { main(args) }
        assertFalse("someone@example.test" in failure.toString())
        assertEquals(fixture, out.readText())
        assertFailsWith<IllegalArgumentException> { main(arrayOf("fixture.app", source.path, source.path)) }
        assertFailsWith<IllegalArgumentException> { main(arrayOf("../elsewhere", source.path, out.path)) }
    }
}
