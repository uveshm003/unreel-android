plugins {
    alias(libs.plugins.unreel.android.library.compose)
}

dependencies {
    api(libs.androidx.compose.material3)
    api(libs.androidx.compose.ui)
    testImplementation(projects.core.testing)
}
