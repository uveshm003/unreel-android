plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.android.lint) apply false
    alias(libs.plugins.android.test) apply false
    alias(libs.plugins.compose) apply false
    alias(libs.plugins.hilt) apply false
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.roborazzi) apply false
    alias(libs.plugins.room) apply false
    alias(libs.plugins.detekt)
    alias(libs.plugins.kover)
    alias(libs.plugins.spotless)
}

/** Modules that ship code (everything except device-only test modules). */
val codeModules = subprojects
    .filter { it.buildFile.exists() }
    .map { it.path }
    .filterNot { it in setOf(":baselineprofile", ":benchmark") }

dependencies {
    codeModules.forEach { kover(project(it)) }
}

// Aggregate coverage gate (docs/PROJECT_BRIEF.md §3). Per-module gates live in the convention plugins.
kover {
    reports {
        verify {
            rule("Overall line coverage") {
                minBound(60)
            }
        }
    }
}

spotless {
    val ktlintVersion = libs.versions.ktlint.get()
    val composeRules = "io.nlopez.compose.rules:ktlint:${libs.versions.composeRules.get()}"
    kotlin {
        target("**/*.kt")
        targetExclude("**/build/**/*.kt")
        ktlint(ktlintVersion).customRuleSets(listOf(composeRules))
    }
    kotlinGradle {
        target("**/*.kts")
        targetExclude("**/build/**/*.kts")
        ktlint(ktlintVersion)
    }
    format("xml") {
        target("**/*.xml")
        targetExclude("**/build/**/*.xml", ".idea/**")
        trimTrailingWhitespace()
        endWithNewline()
    }
}

detekt {
    buildUponDefaultConfig = true
    parallel = true
    config.setFrom(files("config/detekt/detekt.yml"))
    source.setFrom(
        fileTree(rootDir) {
            include("**/src/**/*.kt", "build-logic/**/*.kt")
            exclude("**/build/**")
        },
    )
}

tasks.withType<io.gitlab.arturbosch.detekt.Detekt>().configureEach {
    jvmTarget = libs.versions.jvmTarget.get()
    reports {
        html.required.set(true)
        sarif.required.set(true)
        xml.required.set(false)
        txt.required.set(false)
        md.required.set(false)
    }
}

val checkModuleGraph = tasks.register<Exec>("checkModuleGraph") {
    group = "verification"
    description = "Fails if the Mermaid module graph in README.md / docs/architecture.md is out of date."
    commandLine("python3", "scripts/module-graph.py", "--check")
    inputs.files(
        fileTree(rootDir) {
            include("**/build.gradle.kts", "settings.gradle.kts", "README.md", "docs/architecture.md")
            include("scripts/module-graph.py", "build-logic/**/AndroidFeatureConventionPlugin.kt")
            exclude("**/build/**", ".gradle/**")
        },
    )
}

tasks.register("ciCheck") {
    group = "verification"
    description = "Runs exactly what CI runs: formatting, static analysis, lint, tests, screenshots, coverage, privacy."
    dependsOn("spotlessCheck", "detekt", "koverVerify", checkModuleGraph)
    dependsOn(subprojects.filter { it.buildFile.exists() }.map { "${it.path}:moduleCheck" })
}
