package io.github.uveshm003.unreel.feature.insights

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import io.github.uveshm003.unreel.core.ui.DestinationPlaceholder
import io.github.uveshm003.unreel.core.ui.TopLevelDestination
import kotlinx.serialization.Serializable

@Serializable
data object InsightsRoute : TopLevelDestination {
    override val label: Int get() = R.string.insights_label
    override val icon: Int get() = R.drawable.ic_insights
}

@Serializable
private data object InsightsOverview

fun NavGraphBuilder.insightsScreen() {
    navigation<InsightsRoute>(startDestination = InsightsOverview) {
        composable<InsightsOverview> { InsightsScreen() }
    }
}

@Composable
internal fun InsightsScreen() {
    DestinationPlaceholder(stringResource(InsightsRoute.label), stringResource(R.string.insights_description))
}
