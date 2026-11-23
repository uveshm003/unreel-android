package io.github.uveshm003.unreel.feature.home

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import io.github.uveshm003.unreel.core.ui.DestinationPlaceholder
import io.github.uveshm003.unreel.core.ui.TopLevelDestination
import kotlinx.serialization.Serializable

@Serializable
data object HomeRoute : TopLevelDestination {
    override val label: Int get() = R.string.home_label
    override val icon: Int get() = R.drawable.ic_home
}

@Serializable
private data object HomeOverview

fun NavGraphBuilder.homeScreen() {
    navigation<HomeRoute>(startDestination = HomeOverview) {
        composable<HomeOverview> { HomeScreen() }
    }
}

@Composable
internal fun HomeScreen() {
    DestinationPlaceholder(stringResource(HomeRoute.label), stringResource(R.string.home_description))
}
