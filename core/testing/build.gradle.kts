plugins {
    alias(libs.plugins.unreel.android.library)
}

// Shared test support (MainDispatcherRule, fakes, Roborazzi helpers arrive with the M0 issue). Only test
// source sets depend on this module.
dependencies {
    api(projects.core.common)
    api(projects.core.model)

    api(libs.junit4)
    api(libs.kotlinx.coroutines.test)
    api(libs.turbine)
}
