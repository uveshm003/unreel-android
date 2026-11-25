package io.github.uveshm003.unreel.core.detection

import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.intOrNull

object RulesParser {
    fun parse(source: String): DetectionRules {
        val root =
            Json.parseToJsonElement(source) as? JsonObject ?: throw IllegalArgumentException("Expected a rules object")
        val version = root["schemaVersion"] as? JsonPrimitive
        require(version?.isString == false && version.intOrNull == 1) {
            "Unsupported schemaVersion: $version (expected 1)"
        }
        val file = try {
            Json.decodeFromJsonElement<DetectionRules>(root)
        } catch (error: SerializationException) {
            throw IllegalArgumentException("Invalid rule or matcher: ${error.message}", error)
        }
        require(file.packageName.isNotBlank()) { "package must not be blank" }
        require(file.rulesVersion > 0) { "rulesVersion must be positive" }
        val ids = mutableSetOf<String>()
        for (rule in file.rules) {
            require(rule.id.isNotBlank()) { "Rule id must not be blank" }
            require(ids.add(rule.id)) { "Duplicate rule id: ${rule.id}" }
            require(rule.surface != DetectedSurface.None) { "Rule ${rule.id}: None is reserved for no match" }
            rule.versionCode?.let { validateRange(it, rule.id) }
            validate(rule.match, rule.id)
        }
        return file
    }

    private fun validateRange(range: VersionRange, id: String) {
        val low = range.min ?: 0
        val high = range.max ?: Long.MAX_VALUE
        require(low >= 0 && low <= high) { "Rule $id: invalid versionCode range ${range.min}..${range.max}" }
    }

    private fun validate(matcher: Matcher, id: String) {
        when (matcher) {
            is Matcher.ViewId -> require(matcher.value.isNotBlank()) { "Rule $id: empty viewId" }

            is Matcher.ClassName -> require(matcher.value.isNotBlank()) { "Rule $id: empty className" }

            is Matcher.ContentDescription -> require(matcher.value.isNotBlank()) {
                "Rule $id: empty contentDescription"
            }

            is Matcher.Flags -> require(
                matcher.visible != null || matcher.scrollable != null || matcher.selected != null,
            ) {
                "Rule $id: flags must specify visible, scrollable or selected"
            }

            is Matcher.HasChild -> validate(matcher.matcher, id)

            is Matcher.HasDescendant -> validate(matcher.matcher, id)

            is Matcher.Not -> validate(matcher.matcher, id)

            is Matcher.All -> validateChildren(matcher.matchers, id)

            is Matcher.AnyOf -> validateChildren(matcher.matchers, id)
        }
    }

    private fun validateChildren(children: List<Matcher>, id: String) {
        require(children.isNotEmpty()) { "Rule $id: matchers must not be empty" }
        children.forEach { validate(it, id) }
    }
}
