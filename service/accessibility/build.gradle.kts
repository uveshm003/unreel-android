plugins {
    alias(libs.plugins.unreel.android.library)
    alias(libs.plugins.unreel.hilt)
}

// Thin AccessibilityService adapter: builds NodeSnapshots, calls :core:detection and injected policy classes,
// runs actions. No business logic here (docs/PROJECT_BRIEF.md §2).
dependencies {
    implementation(projects.core.common)
    implementation(projects.core.data)
    implementation(projects.core.detection)
    testImplementation(libs.robolectric)
    testImplementation(libs.androidx.test.core)
}

android.sourceSets.getByName("debug").assets.srcDir(rootProject.file("core/detection/src/main/rules"))
