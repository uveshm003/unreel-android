package io.github.uveshm003.unreel.feature.insights

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import io.github.uveshm003.unreel.core.data.FeedDay
import io.github.uveshm003.unreel.core.designsystem.component.UnreelCard
import io.github.uveshm003.unreel.core.ui.MetricCard
import io.github.uveshm003.unreel.core.ui.OverviewPage
import io.github.uveshm003.unreel.core.ui.TopLevelDestination
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit
import kotlinx.serialization.Serializable

@Serializable
data object InsightsRoute : TopLevelDestination {
    override val label: Int get() = R.string.insights_label
    override val icon: Int get() = R.drawable.ic_insights
}

@Serializable
internal data object InsightsOverview

fun NavGraphBuilder.insightsScreen(days: () -> List<FeedDay> = { emptyList() }) {
    navigation<InsightsRoute>(startDestination = InsightsOverview) {
        composable<InsightsOverview> { InsightsScreen(days()) }
    }
}

@Composable
internal fun InsightsScreen(days: List<FeedDay>) {
    OverviewPage(stringResource(InsightsRoute.label), stringResource(R.string.insights_description)) {
        val exits = days.sumOf { it.exits }
        MetricCard(stringResource(R.string.insights_week), if (days.isEmpty()) "—" else exits.toString())
        if (days.none { it.exits > 0 || it.viewingMillis > 0 }) {
            Text(stringResource(R.string.insights_empty))
        } else {
            val format = DateTimeFormatter.ofPattern("EEE, MMM d", LocalConfiguration.current.locales[0])
            val longest = days.maxOf { it.viewingMillis }.coerceAtLeast(1)
            UnreelCard(Modifier.fillMaxWidth()) {
                days.forEach { day ->
                    Column(
                        Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(2.dp),
                    ) {
                        LinearProgressIndicator(
                            progress = { day.viewingMillis.toFloat() / longest },
                            modifier = Modifier.fillMaxWidth(),
                        )
                        Text(day.date.format(format), style = MaterialTheme.typography.titleSmall)
                        Text(
                            stringResource(
                                R.string.insights_day,
                                day.exits,
                                TimeUnit.MILLISECONDS.toMinutes(day.viewingMillis),
                            ),
                        )
                    }
                }
            }
        }
    }
}
