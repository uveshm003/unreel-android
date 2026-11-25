plugins {
    alias(libs.plugins.unreel.android.application)
    alias(libs.plugins.unreel.android.application.compose)
    alias(libs.plugins.unreel.hilt)
}

android {
    namespace = "io.github.uveshm003.unreel"

    buildFeatures.buildConfig = true

    defaultConfig {
        applicationId = "io.github.uveshm003.unreel"
        // CI passes these from the git tag (release.yml); local builds use the defaults.
        versionCode = providers.gradleProperty("unreel.versionCode").map(String::toInt).getOrElse(1)
        versionName = providers.gradleProperty("unreel.versionName").getOrElse("0.0.1-dev")
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    // Release signing only when CI provides the keystore (see .github/workflows/release.yml). No keystore in git.
    val keystorePath = providers.environmentVariable("UNREEL_KEYSTORE_PATH").orNull?.takeIf { it.isNotBlank() }
    if (keystorePath != null) {
        signingConfigs {
            create("release") {
                storeFile = file(keystorePath)
                storePassword = providers.environmentVariable("UNREEL_KEYSTORE_PASSWORD").get()
                keyAlias = providers.environmentVariable("UNREEL_KEY_ALIAS").get()
                keyPassword = providers.environmentVariable("UNREEL_KEY_PASSWORD").get()
            }
        }
    }

    buildTypes {
        debug {
            applicationIdSuffix = ".debug"
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            if (keystorePath != null) signingConfig = signingConfigs.getByName("release")
        }
    }

    androidResources {
        // Per-app language picker (Settings > Apps > Unreel > Language): English + Hindi.
        generateLocaleConfig = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation(libs.androidx.appcompat)
    implementation(projects.core.designsystem)
    implementation(projects.core.ui)
    implementation(projects.core.data)
    implementation(projects.service.accessibility)
    implementation(projects.feature.onboarding)
    implementation(projects.feature.home)
    implementation(projects.feature.apps)
    implementation(projects.feature.schedule)
    implementation(projects.feature.insights)
    implementation(projects.feature.settings)
    implementation(projects.widget)

    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material3.adaptive)
    implementation(libs.androidx.compose.material3.navigationSuite)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)

    debugImplementation(libs.leakcanary.android)

    testImplementation(projects.core.testing)
    testImplementation(libs.hilt.android.testing)
    kspTest(libs.hilt.compiler)

    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.test.runner)
}
