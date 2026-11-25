package io.github.uveshm003.unreel.core.detection.fixtures

import io.github.uveshm003.unreel.core.detection.NodeBounds
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

internal class FixtureSanitiser(private val labels: Set<String>) {
    fun sanitise(source: String, xml: Boolean): String {
        val root = if (xml) xmlSnapshot(source) else Json.parseToJsonElement(source).jsonObject
        return pretty.encodeToString(node(root)) + "\n"
    }

    private fun node(raw: JsonObject): FixtureNode {
        val bounds = requireNotNull(raw["bounds"]) { "Missing bounds" }
        val description = raw.string("contentDescription")?.takeIf { it.isNotEmpty() }
        return FixtureNode(
            viewIdResourceName = raw.string("viewIdResourceName")?.takeIf(resourceId::matches),
            className = raw.string("className")?.takeIf(className::matches),
            contentDescription = description?.let { if (it in labels) it else "[redacted]" },
            isVisibleToUser = raw.boolean("isVisibleToUser"),
            isScrollable = raw.boolean("isScrollable"),
            isSelected = raw.boolean("isSelected"),
            bounds = pretty.decodeFromJsonElement(NodeBounds.serializer(), bounds),
            children = raw["children"]?.jsonArray.orEmpty().map { node(it.jsonObject) },
        )
    }

    private fun JsonObject.string(key: String): String? = this[key]?.takeUnless {
        it == JsonNull
    }?.jsonPrimitive?.content

    private fun JsonObject.boolean(key: String): Boolean = this[key]?.jsonPrimitive?.boolean ?: false

    private companion object {
        val pretty = Json {
            prettyPrint = true
            ignoreUnknownKeys = true
        }
        val resourceId = Regex("(?:[A-Za-z_][A-Za-z0-9_.]*:)?id/[A-Za-z_][A-Za-z0-9_]*")
        val className = Regex("[A-Za-z_$][A-Za-z0-9_$]*(?:\\.[A-Za-z_$][A-Za-z0-9_$]*)*")
    }
}

@Serializable
private data class FixtureNode(
    val viewIdResourceName: String?,
    val className: String?,
    val contentDescription: String?,
    val isVisibleToUser: Boolean,
    val isScrollable: Boolean,
    val isSelected: Boolean,
    val bounds: NodeBounds,
    val children: List<FixtureNode>,
)
