package io.github.uveshm003.unreel.buildlogic

import com.android.build.api.dsl.Lint
import org.gradle.api.Project

/** Lint errors fail the build. No baselines (see CONTRIBUTING.md). AGP 9 always writes all report formats. */

internal fun Project.configureLint(lint: Lint) = with(lint) {
    abortOnError = true
    checkDependencies = false
    checkReleaseBuilds = false
    explainIssues = false
    absolutePaths = false
}
