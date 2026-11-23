plugins {
    alias(libs.plugins.unreel.android.library)
    alias(libs.plugins.compose)
}

android {
    buildFeatures.compose = true
}

// Only test source sets depend on this module.
dependencies {
    api(projects.core.common)
    api(projects.core.model)

    implementation(projects.core.designsystem)
    api(platform(libs.androidx.compose.bom))
    api(libs.androidx.compose.ui.test.junit4)
    api(libs.robolectric)
    implementation(libs.androidx.test.ext.junit)
    implementation(libs.roborazzi)

    api(libs.junit4)
    api(libs.kotlinx.coroutines.test)
    api(libs.turbine)
}
