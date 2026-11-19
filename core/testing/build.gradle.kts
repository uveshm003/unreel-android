plugins {
    alias(libs.plugins.unreel.android.library)
}

// Shared test support: MainDispatcherRule, TestClock (from :core:common test fixtures, so pure-Kotlin modules
// can use it too) and fakes. Screenshot helpers arrive with the Roborazzi issue. Only test source sets depend on
// this module.
dependencies {
    api(projects.core.common)
    api(testFixtures(projects.core.common))
    api(projects.core.model)

    api(libs.junit4)
    api(libs.kotlinx.coroutines.test)
    api(libs.turbine)
}
