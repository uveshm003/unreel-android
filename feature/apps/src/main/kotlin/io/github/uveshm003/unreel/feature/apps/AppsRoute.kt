package io.github.uveshm003.unreel.feature.apps

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import io.github.uveshm003.unreel.core.ui.DestinationPlaceholder
import io.github.uveshm003.unreel.core.ui.TopLevelDestination
import kotlinx.serialization.Serializable

@Serializable
data object AppsRoute : TopLevelDestination {
    override val label: Int get() = R.string.apps_label
    override val icon: Int get() = R.drawable.ic_apps
}

@Serializable
private data object AppsOverview

fun NavGraphBuilder.appsScreen() {
    navigation<AppsRoute>(startDestination = AppsOverview) {
        composable<AppsOverview> { AppsScreen() }
    }
}

@Composable
internal fun AppsScreen() {
    DestinationPlaceholder(stringResource(AppsRoute.label), stringResource(R.string.apps_description))
}
