package io.github.uveshm003.unreel.feature.settings

import androidx.compose.runtime.Composable
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import io.github.uveshm003.unreel.core.ui.TopLevelDestination
import kotlinx.serialization.Serializable

@Serializable
data object SettingsRoute : TopLevelDestination {
    override val label: Int get() = R.string.settings_label
    override val icon: Int get() = R.drawable.ic_settings
}

@Serializable
internal data object SettingsOverview

fun NavGraphBuilder.settingsScreen(content: @Composable () -> Unit) {
    navigation<SettingsRoute>(startDestination = SettingsOverview) {
        composable<SettingsOverview> { content() }
    }
}
