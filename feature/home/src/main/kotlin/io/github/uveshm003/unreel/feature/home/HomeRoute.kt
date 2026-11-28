package io.github.uveshm003.unreel.feature.home

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import io.github.uveshm003.unreel.core.data.FeedDay
import io.github.uveshm003.unreel.core.designsystem.component.PrimaryButton
import io.github.uveshm003.unreel.core.ui.MetricCard
import io.github.uveshm003.unreel.core.ui.OverviewPage
import io.github.uveshm003.unreel.core.ui.TopLevelDestination
import kotlinx.serialization.Serializable

@Serializable
data object HomeRoute : TopLevelDestination {
    override val label: Int get() = R.string.home_label
    override val icon: Int get() = R.drawable.ic_home
}

@Serializable
internal data object HomeOverview

fun NavGraphBuilder.homeScreen(
    today: () -> FeedDay? = {
        null
    },
    openApps: () -> Unit = {},
    status: @Composable () -> Unit = {},
) {
    navigation<HomeRoute>(startDestination = HomeOverview) {
        composable<HomeOverview> { HomeScreen(today(), openApps, status) }
    }
}

@Composable
internal fun HomeScreen(today: FeedDay?, openApps: () -> Unit, status: @Composable () -> Unit) {
    OverviewPage(stringResource(HomeRoute.label), stringResource(R.string.home_description)) {
        status()
        PrimaryButton(stringResource(R.string.home_manage), openApps)
        MetricCard(stringResource(R.string.home_exits), today?.exits?.toString() ?: "—")
    }
}
