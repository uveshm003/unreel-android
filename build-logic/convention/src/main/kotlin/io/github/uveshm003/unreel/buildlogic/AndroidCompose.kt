package io.github.uveshm003.unreel.buildlogic

import com.android.build.api.dsl.CommonExtension
import io.github.takahirom.roborazzi.RoborazziExtension
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.jetbrains.kotlin.compose.compiler.gradle.ComposeCompilerGradlePluginExtension

/** Compose + Roborazzi screenshot testing for any Android module that renders UI. */
internal fun Project.configureAndroidCompose(commonExtension: CommonExtension) {
    pluginManager.apply("org.jetbrains.kotlin.plugin.compose")
    pluginManager.apply("io.github.takahirom.roborazzi")

    commonExtension.buildFeatures.compose = true

    dependencies {
        val bom = libs.lib("androidx-compose-bom")
        add("implementation", platform(bom))
        add("androidTestImplementation", platform(bom))
        add("testImplementation", platform(bom))
        add("implementation", libs.lib("androidx-compose-ui-tooling-preview"))
        add("debugImplementation", libs.lib("androidx-compose-ui-tooling"))
        add("debugImplementation", libs.lib("androidx-compose-ui-test-manifest"))

        add("testImplementation", libs.lib("androidx-compose-ui-test-junit4"))
        add("testImplementation", libs.lib("junit4"))
        add("testImplementation", libs.lib("androidx-test-ext-junit"))
        add("testImplementation", libs.lib("robolectric"))
        // Compose ui-test and Robolectric bring Espresso 3.5, which cannot drive the API 37 InputManager.
        add("testImplementation", libs.lib("androidx-test-espresso-core"))
        add("androidTestImplementation", libs.lib("androidx-test-espresso-core"))
        add("testImplementation", libs.lib("roborazzi"))
        add("testImplementation", libs.lib("roborazzi-compose"))
        add("testImplementation", libs.lib("roborazzi-junit-rule"))
    }

    extensions.configure<ComposeCompilerGradlePluginExtension> {
        val reports = layout.buildDirectory.dir("compose-reports")
        if (providers.gradleProperty("enableComposeCompilerReports").orNull == "true") {
            reportsDestination.set(reports)
            metricsDestination.set(reports)
        }
    }

    // Golden images live next to the tests so they are reviewed in PRs.
    extensions.configure<RoborazziExtension> {
        outputDir.set(layout.projectDirectory.dir("src/test/screenshots"))
    }
}
