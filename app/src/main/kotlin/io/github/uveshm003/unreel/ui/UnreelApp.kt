package io.github.uveshm003.unreel.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.NavigationRailDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfoV2
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuite
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteDefaults
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffoldDefaults
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffoldLayout
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteType
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.window.core.layout.WindowSizeClass
import io.github.uveshm003.unreel.core.data.ProtectionSummary
import io.github.uveshm003.unreel.core.ui.TopLevelDestination
import io.github.uveshm003.unreel.feature.apps.AppsRoute
import io.github.uveshm003.unreel.feature.apps.appsScreen
import io.github.uveshm003.unreel.feature.home.HomeRoute
import io.github.uveshm003.unreel.feature.home.homeScreen
import io.github.uveshm003.unreel.feature.insights.InsightsRoute
import io.github.uveshm003.unreel.feature.insights.insightsScreen
import io.github.uveshm003.unreel.feature.schedule.ScheduleRoute
import io.github.uveshm003.unreel.feature.schedule.scheduleScreen
import io.github.uveshm003.unreel.feature.settings.SettingsRoute
import io.github.uveshm003.unreel.feature.settings.settingsScreen
import java.time.LocalTime

private val destinations = listOf(HomeRoute, AppsRoute, ScheduleRoute, InsightsRoute, SettingsRoute)

@Composable
fun UnreelApp(
    modifier: Modifier = Modifier,
    summary: ProtectionSummary? = null,
    enableSchedule: (Boolean) -> Unit = {},
    setTime: (Boolean, LocalTime) -> Unit = { _, _ -> },
    protectionStatus: @Composable () -> Unit = {},
    settingsContent: @Composable () -> Unit,
) {
    val currentSummary by rememberUpdatedState(summary)
    val currentStatus by rememberUpdatedState(protectionStatus)
    val currentSettings by rememberUpdatedState(settingsContent)
    val navController = rememberNavController()
    val window = currentWindowAdaptiveInfoV2().windowSizeClass
    val layout = if (window.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND)) {
        NavigationSuiteType.NavigationRail
    } else {
        NavigationSuiteType.NavigationBar
    }

    val isRail = layout == NavigationSuiteType.NavigationRail
    val consumedInsets = if (isRail) {
        NavigationRailDefaults.windowInsets.only(WindowInsetsSides.Start)
    } else {
        NavigationBarDefaults.windowInsets.only(WindowInsetsSides.Bottom)
    }
    Surface(
        modifier = modifier,
        color = NavigationSuiteScaffoldDefaults.containerColor,
        contentColor = NavigationSuiteScaffoldDefaults.contentColor,
    ) {
        NavigationSuiteScaffoldLayout(
            navigationSuiteType = layout,
            navigationSuite = {
                DestinationNavigation(layout, navController)
            },
        ) {
            NavHost(
                navController,
                startDestination = HomeRoute,
                modifier = Modifier.consumeWindowInsets(consumedInsets).safeDrawingPadding(),
            ) {
                homeScreen({
                    currentSummary?.days?.firstOrNull()
                }, { navController.selectTab(AppsRoute) }) { currentStatus() }
                appsScreen()
                scheduleScreen({ currentSummary?.window }, enableSchedule, setTime)
                insightsScreen { currentSummary?.days.orEmpty() }
                settingsScreen { currentSettings() }
            }
        }
    }
}

@Composable
private fun DestinationNavigation(layout: NavigationSuiteType, navController: NavHostController) {
    val entry by navController.currentBackStackEntryAsState()
    val navModifier = if (layout == NavigationSuiteType.NavigationRail) {
        Modifier.fillMaxHeight()
            .background(NavigationSuiteDefaults.colors().navigationRailContainerColor)
            .windowInsetsPadding(NavigationRailDefaults.windowInsets)
            .verticalScroll(rememberScrollState())
    } else {
        Modifier
    }
    NavigationSuite(layoutType = layout, modifier = navModifier) {
        destinations.forEach { route ->
            item(
                selected = entry?.destination?.hierarchy?.any { it.hasRoute(route::class) } == true,
                onClick = { navController.selectTab(route) },
                icon = { Icon(painterResource(route.icon), contentDescription = null) },
                label = {
                    Text(stringResource(route.label), maxLines = 1, overflow = TextOverflow.Ellipsis)
                },
            )
        }
    }
}

internal fun NavHostController.selectTab(route: TopLevelDestination) {
    val currentGraph = currentDestination?.hierarchy?.filterIsInstance<NavGraph>()?.firstOrNull {
        it.hasRoute(route::class)
    }
    if (currentGraph != null) {
        popBackStack(currentGraph.findStartDestination().id, inclusive = false)
    } else {
        navigate(route) {
            popUpTo(graph.findStartDestination().id) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    }
}
