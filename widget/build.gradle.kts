plugins {
    alias(libs.plugins.unreel.android.library.compose)
    alias(libs.plugins.unreel.hilt)
}

// Glance home-screen widget + Quick Settings tile.
dependencies {
    implementation(projects.core.data)
    implementation(projects.core.designsystem)
}
