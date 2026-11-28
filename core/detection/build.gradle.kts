plugins {
    alias(libs.plugins.unreel.jvm.library)
    alias(libs.plugins.kotlin.serialization)
}

// Pure Kotlin/JVM: no Android imports. Rules are JSON data (docs/PROJECT_BRIEF.md §2); fixtures live in
// src/test/resources/fixtures/<app>/<versionCode>/. Coverage gate (>= 80%) comes from unreel.jvm.library.
dependencies {
    api(projects.core.model)
    implementation(libs.kotlinx.serialization.json)
}

tasks.register<JavaExec>("sanitiseFixture") {
    description = "Convert a local XML or JSON snapshot to a sanitised JSON fixture."
    classpath = sourceSets.main.get().runtimeClasspath
    mainClass.set("io.github.uveshm003.unreel.core.detection.fixtures.FixtureCliKt")
    workingDir = rootProject.projectDir
}

sourceSets.test { resources.srcDir("src/main/rules") }
