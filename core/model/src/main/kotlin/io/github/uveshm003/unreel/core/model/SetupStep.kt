package io.github.uveshm003.unreel.core.model

enum class SetupStep(val key: String) {
    Welcome("welcome"),
    Disclosure("disclosure"),
    Service("service"),
    Guides("guides"),
    Complete("complete"),
    ;

    companion object {
        fun fromKey(key: String?): SetupStep = entries.find { it.key == key } ?: Welcome
    }
}
