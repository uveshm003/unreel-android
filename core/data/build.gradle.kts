plugins {
    alias(libs.plugins.unreel.android.library)
    alias(libs.plugins.unreel.hilt)
}

dependencies {
    api(projects.core.model)
    api(projects.core.common)
    implementation(projects.core.database)
    implementation(projects.core.datastore)
    testImplementation(libs.androidx.room.runtime)
    testImplementation(libs.androidx.test.core)
    testImplementation(libs.robolectric)
}
