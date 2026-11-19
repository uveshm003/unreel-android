package io.github.uveshm003.unreel.buildlogic

import kotlinx.kover.gradle.plugin.dsl.KoverProjectExtension
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

/**
 * Coverage instrumentation for a module. Only generated code and IDE previews are excluded; the
 * per-module and aggregate thresholds live in the module build files and the root build file.
 */
internal fun Project.configureKover() {
    pluginManager.apply("org.jetbrains.kotlinx.kover")
    extensions.configure<KoverProjectExtension> {
        reports {
            filters {
                excludes {
                    classes(
                        // Android generated
                        "*.BuildConfig",
                        "*.R",
                        "*.R$*",
                        // Hilt / Dagger generated
                        "*_Factory",
                        "*_Factory$*",
                        "*Factory\$InstanceHolder",
                        "*_MembersInjector",
                        "*_HiltModules*",
                        "*_GeneratedInjector",
                        "*_ComponentTreeDeps",
                        "*_HiltComponents*",
                        "Hilt_*",
                        "*.Hilt_*",
                        "dagger.hilt.internal.*",
                        "hilt_aggregated_deps.*",
                        // Room generated
                        "*_Impl",
                        "*_Impl$*",
                        // Compose compiler generated lambda holders
                        "*ComposableSingletons*",
                    )
                    annotatedBy(
                        "androidx.compose.ui.tooling.preview.Preview",
                        "androidx.compose.ui.tooling.preview.PreviewLightDark",
                        "androidx.compose.ui.tooling.preview.PreviewScreenSizes",
                        "dagger.internal.DaggerGenerated",
                        "javax.annotation.processing.Generated",
                    )
                }
            }
        }
    }
}

/** Fails `koverVerify` for this module if line coverage drops below [minPercent]. */
fun Project.koverMinimumCoverage(minPercent: Int) {
    extensions.configure<KoverProjectExtension> {
        reports {
            verify {
                rule("$path line coverage") {
                    minBound(minPercent)
                }
            }
        }
    }
}
