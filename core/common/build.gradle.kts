plugins {
    alias(libs.plugins.unreel.jvm.library)
    alias(libs.plugins.unreel.hilt)
}

dependencies {
    api(libs.kotlinx.coroutines.core)
}
