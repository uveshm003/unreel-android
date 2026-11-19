package io.github.uveshm003.unreel.buildlogic

import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.getByType

internal val Project.libs: VersionCatalog
    get() = extensions.getByType<VersionCatalogsExtension>().named("libs")

internal fun VersionCatalog.version(alias: String): String = findVersion(alias).get().requiredVersion

internal fun VersionCatalog.lib(alias: String) = findLibrary(alias).get()

/** Package/namespace root, derived from the GitHub login (see docs/PROJECT_BRIEF.md §0). */
internal const val NAMESPACE_ROOT = "io.github.uveshm003.unreel"

/** `:core:engine` -> `io.github.uveshm003.unreel.core.engine` */
internal fun Project.defaultNamespace(): String = NAMESPACE_ROOT + path.replace(':', '.').replace('-', '_')
