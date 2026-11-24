package io.github.uveshm003.unreel.feature.settings

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import io.github.uveshm003.unreel.core.ui.DestinationPlaceholder
import io.github.uveshm003.unreel.core.ui.TopLevelDestination
import kotlinx.serialization.Serializable

@Serializable
data object SettingsRoute : TopLevelDestination {
    override val label: Int get() = R.string.settings_label
    override val icon: Int get() = R.drawable.ic_settings
}

@Serializable
internal data object SettingsOverview

fun NavGraphBuilder.settingsScreen() {
    navigation<SettingsRoute>(startDestination = SettingsOverview) {
        composable<SettingsOverview> { SettingsScreen() }
    }
}

@Composable
internal fun SettingsScreen() {
    DestinationPlaceholder(stringResource(SettingsRoute.label), stringResource(R.string.settings_description))
}
