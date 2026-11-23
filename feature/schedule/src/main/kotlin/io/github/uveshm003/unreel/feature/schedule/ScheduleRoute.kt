package io.github.uveshm003.unreel.feature.schedule

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import io.github.uveshm003.unreel.core.ui.DestinationPlaceholder
import io.github.uveshm003.unreel.core.ui.TopLevelDestination
import kotlinx.serialization.Serializable

@Serializable
data object ScheduleRoute : TopLevelDestination {
    override val label: Int get() = R.string.schedule_label
    override val icon: Int get() = R.drawable.ic_schedule
}

@Serializable
private data object ScheduleOverview

fun NavGraphBuilder.scheduleScreen() {
    navigation<ScheduleRoute>(startDestination = ScheduleOverview) {
        composable<ScheduleOverview> { ScheduleScreen() }
    }
}

@Composable
internal fun ScheduleScreen() {
    DestinationPlaceholder(stringResource(ScheduleRoute.label), stringResource(R.string.schedule_description))
}
