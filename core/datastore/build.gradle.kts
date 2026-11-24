plugins {
    alias(libs.plugins.unreel.android.library)
    alias(libs.plugins.unreel.hilt)
}

dependencies {
    api(projects.core.model)
    api(libs.androidx.dataStore.preferences)
    implementation(projects.core.common)
}
