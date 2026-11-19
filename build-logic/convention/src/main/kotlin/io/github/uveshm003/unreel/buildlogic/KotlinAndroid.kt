package io.github.uveshm003.unreel.buildlogic

import com.android.build.api.dsl.CommonExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.tasks.testing.Test
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask
import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile

private val JAVA_VERSION = JavaVersion.VERSION_17

/** Shared Android config. AGP 9 compiles Kotlin itself ("built-in Kotlin"), so no kotlin-android plugin. */
internal fun Project.configureKotlinAndroid(commonExtension: CommonExtension) {
    commonExtension.apply {
        compileSdk {
            version = release(libs.version("compileSdk").toInt()) {
                minorApiLevel = libs.version("compileSdkMinor").toInt()
            }
        }
        defaultConfig.minSdk = libs.version("minSdk").toInt()
        compileOptions.sourceCompatibility = JAVA_VERSION
        compileOptions.targetCompatibility = JAVA_VERSION
        testOptions.unitTests.isIncludeAndroidResources = true
        testOptions.unitTests.isReturnDefaultValues = false
    }
    configureKotlin()
    configureTests()
}

internal fun Project.configureKotlinJvm() {
    extensions.configure<JavaPluginExtension> {
        sourceCompatibility = JAVA_VERSION
        targetCompatibility = JAVA_VERSION
    }
    configureKotlin()
    configureTests()
}

private fun Project.configureKotlin() {
    tasks.withType<KotlinJvmCompile>().configureEach {
        compilerOptions.jvmTarget.set(JvmTarget.fromTarget(libs.version("jvmTarget")))
    }
    tasks.withType<KotlinCompilationTask<*>>().configureEach {
        compilerOptions {
            allWarningsAsErrors.set(providers.gradleProperty("warningsAsErrors").map(String::toBoolean).orElse(false))
            freeCompilerArgs.addAll(
                "-opt-in=kotlinx.coroutines.ExperimentalCoroutinesApi",
                "-Xconsistent-data-class-copy-visibility",
            )
        }
    }
}

private fun Project.configureTests() {
    // Hilt's bytecode transform emits an empty test jar even in modules without tests, which trips
    // Gradle's "no tests discovered" guard. Keep the guard wherever a module actually has tests.
    val hasTestSources = listOf("src/test/kotlin", "src/test/java").any {
        layout.projectDirectory.dir(it).asFile.exists()
    }
    tasks.withType<Test>().configureEach {
        failOnNoDiscoveredTests.set(hasTestSources)
        // Roborazzi/Robolectric: native graphics with the default synchronous software renderer.
        // `robolectric.pixelCopyRenderMode=hardware` renders asynchronously: with the v2 Compose test rules it
        // captured blank or previous-test frames even after waitForIdle() (4 record runs, 4 different results).
        // Trade-off: software rendering draws no elevation shadows. Follow-up: #84.
        systemProperty("robolectric.graphicsMode", "NATIVE")
        maxParallelForks = (Runtime.getRuntime().availableProcessors() / 2).coerceAtLeast(1)
        // Screenshot tests hold several full-resolution bitmaps (goldens, captures, diffs) per test; the 512 MB
        // Gradle default left later captures in a class blank under ciCheck load.
        maxHeapSize = "2g"
    }
}
