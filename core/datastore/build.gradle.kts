plugins {
    alias(libs.plugins.unreel.android.library)
    alias(libs.plugins.unreel.hilt)
}

dependencies {
    api(projects.core.model)
    implementation(projects.core.common)
}
