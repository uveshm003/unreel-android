package io.github.uveshm003.unreel.buildlogic

import com.android.build.api.artifact.SingleArtifact
import com.android.build.api.variant.ApplicationAndroidComponentsExtension
import javax.xml.parsers.DocumentBuilderFactory
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.register

private const val ANDROID_NS = "http://schemas.android.com/apk/res/android"
private val FORBIDDEN_PERMISSIONS = setOf("android.permission.INTERNET")

/**
 * Unreel is on-device only: the merged *release* manifest must never request INTERNET.
 * If a dependency adds it, the build fails. Do not strip it with `tools:node="remove"`;
 * raise it on the issue instead (docs/PROJECT_BRIEF.md §3).
 */
@CacheableTask
abstract class CheckNoInternetPermissionTask : DefaultTask() {
    @get:InputFile
    @get:PathSensitive(PathSensitivity.NONE)
    abstract val mergedManifest: RegularFileProperty

    @get:OutputFile
    abstract val report: RegularFileProperty

    @TaskAction
    fun check() {
        val factory = DocumentBuilderFactory.newInstance().apply { isNamespaceAware = true }
        val document = factory.newDocumentBuilder().parse(mergedManifest.get().asFile)
        val requested = listOf("uses-permission", "uses-permission-sdk-23").flatMap { tag ->
            val nodes = document.getElementsByTagName(tag)
            (0 until nodes.length).map { i ->
                nodes.item(i).attributes.getNamedItemNS(ANDROID_NS, "name")?.nodeValue.orEmpty()
            }
        }
        val violations = requested.filter { it in FORBIDDEN_PERMISSIONS }
        if (violations.isNotEmpty()) {
            throw GradleException(
                "Privacy check failed: the merged release manifest requests $violations. " +
                    "Unreel must never have network access. Find the dependency that adds it " +
                    "(build/intermediates/merged_manifest/) and raise it on the issue instead of stripping it.",
            )
        }
        report.get().asFile.writeText("OK: no forbidden permissions in ${requested.size} requested permissions\n")
    }
}

internal fun Project.registerNoInternetPermissionCheck() {
    val aggregate = tasks.register("checkNoInternetPermission") {
        group = "verification"
        description = "Fails if a release manifest requests android.permission.INTERNET."
    }
    extensions.configure<ApplicationAndroidComponentsExtension> {
        onVariants(selector().withBuildType("release")) { variant ->
            val name = "check${variant.name.replaceFirstChar(Char::titlecase)}NoInternetPermission"
            val check = tasks.register<CheckNoInternetPermissionTask>(name) {
                group = "verification"
                mergedManifest.set(variant.artifacts.get(SingleArtifact.MERGED_MANIFEST))
                report.set(layout.buildDirectory.file("reports/privacy/${variant.name}-no-internet.txt"))
            }
            aggregate.configure { dependsOn(check) }
        }
    }
}
