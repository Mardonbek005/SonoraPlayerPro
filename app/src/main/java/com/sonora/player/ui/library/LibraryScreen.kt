package com.sonora.player.ui.library

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.sonora.player.R
import com.sonora.player.domain.model.Song
import com.sonora.player.ui.common.AudioPermissionGate
import com.sonora.player.ui.common.SongRow

@Composable
fun LibraryScreen(
    onSongClick: (Song) -> Unit,
    viewModel: LibraryViewModel = hiltViewModel()
) {
    AudioPermissionGate(onPermissionGranted = { viewModel.refresh() }) {
        val uiState by viewModel.uiState.collectAsState()

        Column(modifier = Modifier.fillMaxSize()) {
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = viewModel::onSearchQueryChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                placeholder = { Text(stringResourceCompat(R.string.search_hint)) },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                singleLine = true
            )

            if (uiState.songs.isEmpty() && !uiState.isLoading) {
                EmptyLibraryMessage()
            } else {
                LazyColumn {
                    items(uiState.songs, key = { it.id }) { song ->
                        SongRow(
                            song = song,
                            onClick = { onSongClick(song); viewModel.playSong(song) },
                            onFavoriteClick = { viewModel.toggleFavorite(song.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyLibraryMessage() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Filled.MusicNote,
            contentDescription = null,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        Text(stringResourceCompat(R.string.empty_library_title), style = MaterialTheme.typography.titleMedium)
        Text(stringResourceCompat(R.string.empty_library_body), style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun stringResourceCompat(resId: Int): String =
    androidx.compose.ui.res.stringResource(resId)
