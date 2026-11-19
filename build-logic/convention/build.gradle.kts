import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    `kotlin-dsl`
}

group = "io.github.uveshm003.unreel.buildlogic"

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_17
    }
}

dependencies {
    compileOnly(libs.android.gradlePlugin)
    compileOnly(libs.compose.gradlePlugin)
    compileOnly(libs.kotlin.gradlePlugin)
    compileOnly(libs.kover.gradlePlugin)
    compileOnly(libs.ksp.gradlePlugin)
    compileOnly(libs.roborazzi.gradlePlugin)
    compileOnly(libs.room.gradlePlugin)
}

tasks {
    validatePlugins {
        enableStricterValidation = true
        failOnWarning = true
    }
}

gradlePlugin {
    plugins {
        register("androidApplication") {
            id = libs.plugins.unreel.android.application.asProvider().get().pluginId
            implementationClass = "io.github.uveshm003.unreel.buildlogic.AndroidApplicationConventionPlugin"
        }
        register("androidApplicationCompose") {
            id = libs.plugins.unreel.android.application.compose.get().pluginId
            implementationClass = "io.github.uveshm003.unreel.buildlogic.AndroidApplicationComposeConventionPlugin"
        }
        register("androidLibrary") {
            id = libs.plugins.unreel.android.library.asProvider().get().pluginId
            implementationClass = "io.github.uveshm003.unreel.buildlogic.AndroidLibraryConventionPlugin"
        }
        register("androidLibraryCompose") {
            id = libs.plugins.unreel.android.library.compose.get().pluginId
            implementationClass = "io.github.uveshm003.unreel.buildlogic.AndroidLibraryComposeConventionPlugin"
        }
        register("androidFeature") {
            id = libs.plugins.unreel.android.feature.get().pluginId
            implementationClass = "io.github.uveshm003.unreel.buildlogic.AndroidFeatureConventionPlugin"
        }
        register("androidTest") {
            id = libs.plugins.unreel.android.test.get().pluginId
            implementationClass = "io.github.uveshm003.unreel.buildlogic.AndroidTestConventionPlugin"
        }
        register("androidRoom") {
            id = libs.plugins.unreel.android.room.get().pluginId
            implementationClass = "io.github.uveshm003.unreel.buildlogic.AndroidRoomConventionPlugin"
        }
        register("hilt") {
            id = libs.plugins.unreel.hilt.get().pluginId
            implementationClass = "io.github.uveshm003.unreel.buildlogic.HiltConventionPlugin"
        }
        register("jvmLibrary") {
            id = libs.plugins.unreel.jvm.library.get().pluginId
            implementationClass = "io.github.uveshm003.unreel.buildlogic.JvmLibraryConventionPlugin"
        }
    }
}
