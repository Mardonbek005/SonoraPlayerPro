package com.sonora.player.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.weight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.sonora.player.ui.favorites.FavoritesScreen
import com.sonora.player.ui.library.LibraryScreen
import com.sonora.player.ui.player.MiniPlayer
import com.sonora.player.ui.player.PlayerScreen
import com.sonora.player.ui.playlist.PlaylistDetailScreen
import com.sonora.player.ui.playlist.PlaylistScreen
import com.sonora.player.ui.settings.SettingsScreen

private data class TabInfo(val destination: Destination, val label: String, val icon: ImageVector)

private val tabs = listOf(
    TabInfo(Destination.Library, "Kutubxona", Icons.Filled.LibraryMusic),
    TabInfo(Destination.Playlists, "Pleylistlar", Icons.Filled.QueueMusic),
    TabInfo(Destination.Favorites, "Sevimlilar", Icons.Filled.Favorite),
    TabInfo(Destination.Settings, "Sozlamalar", Icons.Filled.Settings)
)

@Composable
fun SonoraNavHost() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination

    val isTopLevelRoute = tabs.any { it.destination.route == currentRoute?.route }

    Scaffold(
        bottomBar = {
            if (isTopLevelRoute) {
                NavigationBar {
                    tabs.forEach { tab ->
                        NavigationBarItem(
                            selected = currentRoute?.hierarchy?.any { it.route == tab.destination.route } == true,
                            onClick = {
                                navController.navigate(tab.destination.route) {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(tab.icon, contentDescription = tab.label) },
                            label = { Text(tab.label) }
                        )
                    }
                }
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            Box(modifier = Modifier.weight(1f, fill = true)) {
                NavHost(navController = navController, startDestination = Destination.Library.route) {
                    composable(Destination.Library.route) {
                        LibraryScreen(onSongClick = { navController.navigate(Destination.Player.route) })
                    }
                    composable(Destination.Playlists.route) {
                        PlaylistScreen(onPlaylistClick = {
                            navController.navigate(Destination.PlaylistDetail.createRoute(it.id))
                        })
                    }
                    composable(Destination.Favorites.route) {
                        FavoritesScreen(onSongClick = { navController.navigate(Destination.Player.route) })
                    }
                    composable(Destination.Settings.route) {
                        SettingsScreen()
                    }
                    composable(Destination.Player.route) {
                        PlayerScreen(onBack = { navController.popBackStack() })
                    }
                    composable(
                        route = Destination.PlaylistDetail.route,
                        arguments = listOf(navArgument("playlistId") { type = NavType.LongType })
                    ) {
                        PlaylistDetailScreen(onSongClick = { navController.navigate(Destination.Player.route) })
                    }
                }
            }

            if (isTopLevelRoute) {
                Box(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                    MiniPlayer(onExpand = { navController.navigate(Destination.Player.route) })
                }
            }
        }
    }
}
