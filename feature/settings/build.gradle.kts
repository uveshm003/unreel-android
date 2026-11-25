plugins {
    alias(libs.plugins.unreel.android.feature)
}

dependencies {
    implementation(libs.androidx.appcompat)
    implementation(projects.core.data)
    testImplementation(libs.androidx.dataStore.preferences)
}
