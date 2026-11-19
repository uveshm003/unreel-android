package io.github.uveshm003.unreel.buildlogic

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

/**
 * Hilt via KSP, for Android modules (hilt-android + the Hilt Gradle plugin) and pure Kotlin/JVM modules
 * (hilt-core: `@Module`/`@InstallIn` only). Modules whose tests need Hilt add `hilt-android-testing` +
 * `kspTest(hilt-compiler)` themselves, so modules without Hilt tests do not get generated test sources.
 */
class HiltConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("com.google.devtools.ksp")
            dependencies { add("ksp", libs.lib("hilt-compiler")) }

            var supported = false
            pluginManager.withPlugin("org.jetbrains.kotlin.jvm") {
                supported = true
                dependencies { add("api", libs.lib("hilt-core")) }
            }
            listOf("com.android.application", "com.android.library").forEach { androidPlugin ->
                pluginManager.withPlugin(androidPlugin) {
                    supported = true
                    pluginManager.apply("com.google.dagger.hilt.android")
                    dependencies { add("implementation", libs.lib("hilt-android")) }
                }
            }
            afterEvaluate {
                check(supported) {
                    "unreel.hilt supports Android application/library and Kotlin JVM modules only ($path)"
                }
            }
        }
    }
}
