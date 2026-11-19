package io.github.uveshm003.unreel.buildlogic

import org.gradle.api.Project

/**
 * Every module registers `moduleCheck`; the root `ciCheck` depends on all of them. Keeping the
 * per-module wiring here means a new module is gated automatically.
 */
internal fun Project.registerModuleCheck(vararg taskNames: String) {
    tasks.register(MODULE_CHECK_TASK) {
        group = "verification"
        description = "Runs this module's tests, lint, screenshot and coverage checks."
        dependsOn(taskNames.toList())
    }
}

internal fun Project.addToModuleCheck(vararg taskNames: String) {
    tasks.named(MODULE_CHECK_TASK) { dependsOn(taskNames.toList()) }
}

const val MODULE_CHECK_TASK = "moduleCheck"
