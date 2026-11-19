package io.github.uveshm003.unreel.buildlogic

import com.android.build.api.dsl.ApplicationExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

class AndroidApplicationConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("com.android.application")
            extensions.configure<ApplicationExtension> {
                configureKotlinAndroid(this)
                defaultConfig.targetSdk = libs.version("targetSdk").toInt()
                testOptions.animationsDisabled = true
                configureLint(lint)
            }
            configureKover()
            registerNoInternetPermissionCheck()
            registerModuleCheck("testDebugUnitTest", "lintDebug", "checkNoInternetPermission")
        }
    }
}
