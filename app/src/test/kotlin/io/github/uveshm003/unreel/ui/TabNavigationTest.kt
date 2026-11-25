package io.github.uveshm003.unreel.ui

import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.navigation.NavGraph
import androidx.navigation.NavHostController
import androidx.navigation.compose.ComposeNavigator
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.github.uveshm003.unreel.feature.apps.AppsRoute
import io.github.uveshm003.unreel.feature.apps.appsScreen
import io.github.uveshm003.unreel.feature.home.HomeRoute
import io.github.uveshm003.unreel.feature.home.homeScreen
import io.github.uveshm003.unreel.feature.settings.SettingsRoute
import io.github.uveshm003.unreel.feature.settings.settingsScreen
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TabNavigationTest {
    @get:Rule val compose = createComposeRule()

    @Test
    fun switchingRestoresDetailAndReselectingPopsToTheTabRoot() {
        lateinit var controller: NavHostController
        compose.setContent {
            controller = rememberNavController()
            NavHost(controller, startDestination = HomeRoute) {
                homeScreen()
                appsScreen()
                settingsScreen {}
            }
        }
        compose.runOnIdle {
            controller.selectTab(AppsRoute)
            val appsRoot = controller.currentDestination?.id
            val appsGraph = controller.graph.findNode(AppsRoute::class) as NavGraph
            val navigator = controller.navigatorProvider.getNavigator(ComposeNavigator::class.java)
            appsGraph.addDestination(ComposeNavigator.Destination(navigator) {}.apply { route = "app-detail" })
            controller.navigate("app-detail")
            controller.currentBackStackEntry!!.savedStateHandle["draft"] = "saved"
            controller.selectTab(SettingsRoute)
            controller.selectTab(AppsRoute)
            assertEquals("app-detail", controller.currentDestination?.route)
            assertEquals("saved", controller.currentBackStackEntry!!.savedStateHandle.get<String>("draft"))
            controller.selectTab(AppsRoute)
            assertEquals(appsRoot, controller.currentDestination?.id)
            controller.selectTab(HomeRoute)
            assertNull(controller.previousBackStackEntry)
        }
    }
}
