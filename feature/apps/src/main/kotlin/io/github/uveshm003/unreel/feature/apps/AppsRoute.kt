package io.github.uveshm003.unreel.feature.apps

import androidx.compose.runtime.Composable
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import io.github.uveshm003.unreel.core.ui.TopLevelDestination
import kotlinx.serialization.Serializable

@Serializable
data object AppsRoute : TopLevelDestination {
    override val label: Int get() = R.string.apps_label
    override val icon: Int get() = R.drawable.ic_apps
}

@Serializable
internal data object AppsOverview

fun NavGraphBuilder.appsScreen(content: @Composable () -> Unit = { AppsDestination() }) {
    navigation<AppsRoute>(startDestination = AppsOverview) {
        composable<AppsOverview> { content() }
    }
}
