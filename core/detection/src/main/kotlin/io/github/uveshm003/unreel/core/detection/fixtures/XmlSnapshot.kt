package io.github.uveshm003.unreel.core.detection.fixtures

import java.io.StringReader
import javax.xml.parsers.DocumentBuilderFactory
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import org.w3c.dom.Element
import org.xml.sax.InputSource
import org.xml.sax.helpers.DefaultHandler

private val boundsPattern = Regex("\\[(-?\\d+),(-?\\d+)]\\[(-?\\d+),(-?\\d+)]")

internal fun xmlSnapshot(source: String): JsonObject {
    val factory = DocumentBuilderFactory.newInstance().apply {
        setFeature("http://apache.org/xml/features/disallow-doctype-decl", true)
    }
    val builder = factory.newDocumentBuilder().apply { setErrorHandler(DefaultHandler()) }
    val root = builder.parse(InputSource(StringReader(source))).documentElement
    val node = when (root.tagName) {
        "node" -> root
        "hierarchy" -> root.childElements().single()
        else -> throw IllegalArgumentException("Expected a node or hierarchy")
    }
    return node.toRawNode()
}

private fun Element.childElements(): List<Element> = buildList {
    for (i in 0 until childNodes.length) {
        val child = childNodes.item(i)
        if (child is Element && child.tagName == "node") add(child)
    }
}

private fun Element.toRawNode(): JsonObject = buildJsonObject {
    put("viewIdResourceName", getAttribute("resource-id"))
    put("className", getAttribute("class"))
    put("contentDescription", getAttribute("content-desc"))
    put(
        "isVisibleToUser",
        if (hasAttribute("visible-to-user")) getAttribute("visible-to-user").toBooleanStrict() else true,
    )
    put("isScrollable", getAttribute("scrollable").ifEmpty { "false" }.toBooleanStrict())
    put("isSelected", getAttribute("selected").ifEmpty { "false" }.toBooleanStrict())
    val coords = requireNotNull(boundsPattern.matchEntire(getAttribute("bounds"))) { "Invalid bounds" }.destructured
    val bounds = listOf("left", "top", "right", "bottom").zip(coords.toList())
    put(
        "bounds",
        buildJsonObject {
            for ((key, value) in bounds) put(key, value.toInt())
        },
    )
    put("children", JsonArray(childElements().map { it.toRawNode() }))
}
