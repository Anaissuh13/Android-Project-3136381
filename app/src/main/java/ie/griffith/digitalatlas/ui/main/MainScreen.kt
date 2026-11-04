package ie.griffith.digitalatlas.ui.main

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import ie.griffith.digitalatlas.R
import ie.griffith.digitalatlas.ui.map.MapScreen
import ie.griffith.digitalatlas.ui.pinned.PinnedScreen
import ie.griffith.digitalatlas.data.model.Place

//keeping both tabs in one enum so the bottom bar and navhost read from the same list
//filled icon when selected and outlined when not, so the current tab is obvious
enum class AtlasTab(
    val route: String,
    @StringRes val labelRes: Int,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    MAP("map", R.string.tab_map, Icons.Filled.Map, Icons.Outlined.Map),
    PINNED("pinned", R.string.tab_pinned, Icons.Filled.PushPin, Icons.Outlined.PushPin)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    onSettingsClick: () -> Unit,
    //passed up to MainActivity, which opens the details screen
    onSeeDetails: (Place) -> Unit
) {
    val navController = rememberNavController()
    //watching the back stack so the bottom bar knows which tab to highlight
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.app_name)) },
                actions = {
                    IconButton(onClick = onSettingsClick) {
                        Icon(
                            imageVector = Icons.Filled.Settings,
                            contentDescription = stringResource(R.string.settings)
                        )
                    }
                },
                //only colouring the title teal, a fully teal bar clashes with the status bar icons
                colors = TopAppBarDefaults.topAppBarColors(
                    titleContentColor = MaterialTheme.colorScheme.primary
                )
            )
        },
        bottomBar = {
            NavigationBar {
                AtlasTab.entries.forEach { tab ->
                    val selected = currentRoute == tab.route
                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            navController.navigate(tab.route) {
                                //going back to the start tab instead of piling up copies of screens,
                                //and saving/restoring state so each tab remembers where it was
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        //label is already shown under the icon so the icon doesn't need its own description
                        icon = {
                            Icon(
                                imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                                contentDescription = null
                            )
                        },
                        label = { Text(stringResource(tab.labelRes)) }
                    )
                }
            }
        }
    ) { innerPadding ->
        //innerPadding stops the screens from going under the top and bottom bars
        NavHost(
            navController = navController,
            startDestination = AtlasTab.MAP.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(AtlasTab.MAP.route) { MapScreen(onSeeDetails = onSeeDetails) }
            composable(AtlasTab.PINNED.route) { PinnedScreen() }
        }
    }
}