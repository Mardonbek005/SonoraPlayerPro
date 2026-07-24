package com.sonora.player.ui.favorites

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.sonora.player.domain.model.Song
import com.sonora.player.ui.common.SongRow

@Composable
fun FavoritesScreen(
    onSongClick: (Song) -> Unit,
    viewModel: FavoritesViewModel = hiltViewModel()
) {
    val favorites by viewModel.favorites.collectAsState()

    if (favorites.isEmpty()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("Sevimli qo'shiqlar yo'q", style = MaterialTheme.typography.titleMedium)
        }
        return
    }

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(favorites, key = { it.id }) { song ->
            SongRow(
                song = song,
                isFavorite = true,
                onClick = {
                    onSongClick(song)
                    viewModel.playFrom(song)
                },
                onFavoriteClick = { viewModel.toggleFavorite(song.id) }
            )
        }
    }
}
