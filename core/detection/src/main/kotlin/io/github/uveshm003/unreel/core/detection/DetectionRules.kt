package io.github.uveshm003.unreel.core.detection

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DetectionRules(
    val schemaVersion: Int,
    @SerialName("package") val packageName: String,
    val rulesVersion: Int,
    val rules: List<DetectionRule>,
)

@Serializable
data class DetectionRule(
    val id: String,
    val surface: DetectedSurface,
    val priority: Int,
    val match: Matcher,
    val versionCode: VersionRange? = null,
)

@Serializable
data class VersionRange(val min: Long? = null, val max: Long? = null) {
    operator fun contains(version: Long): Boolean = (min == null || version >= min) && (max == null || version <= max)
}

@Serializable
sealed interface Matcher {
    @Serializable
    @SerialName("viewId")
    data class ViewId(val value: String) : Matcher

    @Serializable
    @SerialName("className")
    data class ClassName(val value: String) : Matcher

    @Serializable
    @SerialName("contentDescription")
    data class ContentDescription(val value: String) : Matcher

    @Serializable
    @SerialName("flags")
    data class Flags(val visible: Boolean? = null, val scrollable: Boolean? = null, val selected: Boolean? = null) :
        Matcher

    @Serializable
    @SerialName("hasChild")
    data class HasChild(val matcher: Matcher) : Matcher

    @Serializable
    @SerialName("hasDescendant")
    data class HasDescendant(val matcher: Matcher) : Matcher

    @Serializable
    @SerialName("all")
    data class All(val matchers: List<Matcher>) : Matcher

    @Serializable
    @SerialName("any")
    data class AnyOf(val matchers: List<Matcher>) : Matcher

    @Serializable
    @SerialName("not")
    data class Not(val matcher: Matcher) : Matcher
}
