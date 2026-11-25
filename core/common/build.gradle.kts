plugins {
    alias(libs.plugins.unreel.jvm.library)
    alias(libs.plugins.unreel.hilt)
    `java-test-fixtures`
}

dependencies {
    api(libs.kotlinx.coroutines.core)

    testImplementation(libs.kotlinx.coroutines.test)
}
