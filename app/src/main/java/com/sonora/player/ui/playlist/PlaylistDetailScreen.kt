package com.sonora.player.ui.playlist

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.sonora.player.domain.model.Song
import com.sonora.player.ui.common.SongRow

@Composable
fun PlaylistDetailScreen(
    onSongClick: (Song) -> Unit,
    viewModel: PlaylistDetailViewModel = hiltViewModel()
) {
    val songs by viewModel.songs.collectAsState()

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(songs, key = { it.id }) { song ->
            SongRow(
                song = song,
                onClick = {
                    onSongClick(song)
                    viewModel.playFrom(song)
                },
                trailingContent = {
                    IconButton(onClick = { viewModel.removeSong(song.id) }) {
                        Icon(Icons.Filled.Delete, contentDescription = null)
                    }
                }
            )
        }
    }
}
