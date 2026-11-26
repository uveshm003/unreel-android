plugins {
    alias(libs.plugins.unreel.android.feature)
}

android.sourceSets["test"].resources.srcDir(rootProject.file(".github/ISSUE_TEMPLATE"))

dependencies {
    implementation(libs.androidx.appcompat)
    implementation(projects.core.data)
    testImplementation(libs.androidx.dataStore.preferences)
}
