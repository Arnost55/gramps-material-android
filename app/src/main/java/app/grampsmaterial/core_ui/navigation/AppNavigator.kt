package app.grampsmaterial.core_ui.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountTree
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import app.grampsmaterial.R
import app.grampsmaterial.core_network.GrampsServer
import app.grampsmaterial.feature_auth.ConnectionScreen
import app.grampsmaterial.feature_auth.WelcomeScreen
import app.grampsmaterial.feature_home.HomeScreen
import app.grampsmaterial.feature_person.PersonProfileScreen
import app.grampsmaterial.feature_places.PlacesScreen
import app.grampsmaterial.feature_search.SearchScreen
import app.grampsmaterial.feature_settings.SettingsScreen
import app.grampsmaterial.feature_settings.viewmodel.SettingsViewModel
import app.grampsmaterial.feature_tree.TreeSelectionScreen
import app.grampsmaterial.feature_tree.TreeViewerScreen
import app.grampsmaterial.feature_tree.viewmodel.TreeSelectionViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavigator(
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()
    val sessionViewModel: SessionViewModel = hiltViewModel()
    val sessionState by sessionViewModel.state.collectAsState()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    LaunchedEffect(sessionState.isLoading, sessionState.isConnected, sessionState.hasSelectedTree) {
        if (sessionState.isLoading) return@LaunchedEffect
        val destination = if (sessionState.isConnected) {
            if (sessionState.hasSelectedTree) NavDestination.Home.route else NavDestination.TreeSelection.route
        } else {
            NavDestination.Welcome.route
        }
        if (currentRoute != destination) {
            navController.navigate(destination) {
                popUpTo(NavDestination.Welcome.route) { inclusive = true }
                launchSingleTop = true
            }
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(title = { 
                val title = when (currentRoute) {
                    NavDestination.Welcome.route -> stringResource(R.string.welcome_title)
                    NavDestination.Connection.route -> stringResource(R.string.connection_title)
                    NavDestination.TreeSelection.route -> stringResource(R.string.select_tree_title)
                    NavDestination.Home.route -> stringResource(R.string.home_title)
                    NavDestination.Search.route -> stringResource(R.string.search_title)
                    NavDestination.Tree.route -> stringResource(R.string.tree_title)
                    NavDestination.PersonProfile.route + "/{personHandle}" -> stringResource(R.string.person_profile_title)
                    NavDestination.Settings.route -> stringResource(R.string.settings_title)
                    NavDestination.Places.route -> "Places"
                    else -> stringResource(R.string.app_name)
                }
                Text(text = title)
            })
        },
        bottomBar = {
            if (currentRoute in setOf(
                    NavDestination.Home.route,
                    NavDestination.Search.route,
                    NavDestination.Tree.route,
                    NavDestination.Places.route,
                    NavDestination.Settings.route
                )
            ) {
                NavigationBar {
                    listOf(
                        Triple(NavDestination.Home, Icons.Outlined.Home, "Home"),
                        Triple(NavDestination.Search, Icons.Outlined.Search, "Search"),
                        Triple(NavDestination.Tree, Icons.Outlined.AccountTree, "Tree"),
                        Triple(NavDestination.Places, Icons.Outlined.Place, "Places"),
                        Triple(NavDestination.Settings, Icons.Outlined.Settings, "Settings")
                    ).forEach { (destination, icon, label) ->
                        NavigationBarItem(
                            selected = currentRoute == destination.route,
                            onClick = {
                                navController.navigate(destination.route) {
                                    launchSingleTop = true
                                    restoreState = true
                                    popUpTo(NavDestination.Home.route) { saveState = true }
                                }
                            },
                            icon = { androidx.compose.material3.Icon(icon, contentDescription = label) },
                            label = { Text(label) }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = NavDestination.Welcome.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(NavDestination.Welcome.route) {
                WelcomeScreen(
                    onContinue = { navController.navigate(NavDestination.Connection.route) }
                )
            }
            composable(NavDestination.Connection.route) {
                ConnectionScreen(
                    onConnected = { },
                    onBackToWelcome = { navController.popBackStack() }
                )
            }
            composable(NavDestination.TreeSelection.route) {
                val viewModel: TreeSelectionViewModel = hiltViewModel()
                val serverUrl by viewModel.serverUrl.collectAsState()
                TreeSelectionScreen(
                    server = GrampsServer(baseUrl = serverUrl),
                    onTreeSelected = { tree ->
                        navController.navigate(NavDestination.Home.route)
                    },
                    onBack = { navController.popBackStack() },
                    viewModel = viewModel
                )
            }
            composable(NavDestination.Home.route) {
                HomeScreen(
                    onTreeChange = { navController.navigate(NavDestination.TreeSelection.route) },
                    onSearchClick = { navController.navigate(NavDestination.Search.route) },
                    onPersonSelected = { handle -> navController.navigate("${NavDestination.PersonProfile.route}/$handle") },
                    onSettingsClick = { navController.navigate(NavDestination.Settings.route) }
                )
            }
            composable(NavDestination.Search.route) {
                SearchScreen(
                    onBack = { navController.popBackStack() },
                    onPersonSelected = { personHandle ->
                        navController.navigate("${NavDestination.PersonProfile.route}/$personHandle")
                    }
                )
            }
            composable(
                route = "${NavDestination.PersonProfile.route}/{personHandle}",
                arguments = listOf(navArgument("personHandle") { type = NavType.StringType })
            ) { backStackEntry ->
                val personHandle = backStackEntry.arguments?.getString("personHandle") ?: ""
                PersonProfileScreen(
                    personHandle = personHandle,
                    onBack = { navController.popBackStack() },
                    onPersonSelected = { handle ->
                        navController.navigate("${NavDestination.PersonProfile.route}/$handle")
                    }
                )
            }
            composable(NavDestination.Tree.route) {
                TreeViewerScreen(
                    onBack = { navController.popBackStack() },
                    onPersonSelected = { handle ->
                        navController.navigate("${NavDestination.PersonProfile.route}/$handle")
                    }
                )
            }
            composable(NavDestination.Places.route) { PlacesScreen() }
            composable(NavDestination.Settings.route) {
                val settingsViewModel: SettingsViewModel = hiltViewModel()
                SettingsScreen(
                    onBack = { navController.popBackStack() },
                    onEditServer = {
                        settingsViewModel.logout()
                        navController.navigate(NavDestination.Connection.route) {
                            popUpTo(NavDestination.Welcome.route) { inclusive = true }
                        }
                    },
                    onLogout = {
                        settingsViewModel.logout()
                        navController.navigate(NavDestination.Welcome.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }
        }
    }
}
