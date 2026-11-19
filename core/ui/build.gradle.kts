plugins {
    alias(libs.plugins.unreel.android.library.compose)
}

dependencies {
    api(projects.core.designsystem)
    api(projects.core.model)
}
