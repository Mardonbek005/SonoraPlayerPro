package com.sonora.player.ui.navigation

sealed class Destination(val route: String) {
    data object Library : Destination("library")
    data object Playlists : Destination("playlists")
    data object Favorites : Destination("favorites")
    data object Settings : Destination("settings")
    data object Player : Destination("player")
    data object PlaylistDetail : Destination("playlist/{playlistId}") {
        fun createRoute(playlistId: Long) = "playlist/$playlistId"
    }
}

val bottomNavDestinations = listOf(
    Destination.Library,
    Destination.Playlists,
    Destination.Favorites,
    Destination.Settings
)
