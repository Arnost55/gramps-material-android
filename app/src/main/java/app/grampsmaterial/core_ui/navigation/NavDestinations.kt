package app.grampsmaterial.core_ui.navigation

import androidx.annotation.StringRes
import app.grampsmaterial.R

sealed class NavDestination(val route: String, @StringRes val titleRes: Int) {
    data object Welcome : NavDestination(route = "welcome", titleRes = R.string.welcome_title)
    data object Connection : NavDestination(route = "connection", titleRes = R.string.connection_title)
    data object TreeSelection : NavDestination(route = "tree_selection", titleRes = R.string.select_tree_title)
    data object Home : NavDestination(route = "home", titleRes = R.string.home_title)
    data object Search : NavDestination(route = "search", titleRes = R.string.search_title)
    data object Tree : NavDestination(route = "tree", titleRes = R.string.tree_title)
    data object PersonProfile : NavDestination(route = "person_profile", titleRes = R.string.person_profile_title)
    data object Settings : NavDestination(route = "settings", titleRes = R.string.settings_title)
}