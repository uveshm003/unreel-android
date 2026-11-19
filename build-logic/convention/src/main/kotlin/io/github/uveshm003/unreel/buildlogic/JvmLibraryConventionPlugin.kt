package io.github.uveshm003.unreel.buildlogic

import kotlinx.kover.gradle.plugin.dsl.KoverProjectExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies

/** Pure Kotlin/JVM module: no Android dependencies allowed. Coverage gate: >= 80% (docs/PROJECT_BRIEF.md §3). */
class JvmLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("org.jetbrains.kotlin.jvm")
            pluginManager.apply("com.android.lint")
            configureKotlinJvm()
            dependencies {
                add("testImplementation", libs.lib("junit4"))
                add("testImplementation", libs.lib("kotlin-test"))
                add("testImplementation", libs.lib("kotlinx-coroutines-test"))
                add("testImplementation", libs.lib("turbine"))
                add("testImplementation", libs.lib("truth"))
            }
            configureKover()
            // Only production code counts: test fixtures are test support, like src/test.
            extensions.configure<KoverProjectExtension> {
                currentProject { sources { excludedSourceSets.add("testFixtures") } }
            }
            koverMinimumCoverage(PURE_KOTLIN_MIN_COVERAGE)
            registerModuleCheck("test", "lint", "koverVerify")
        }
    }
}

private const val PURE_KOTLIN_MIN_COVERAGE = 80
