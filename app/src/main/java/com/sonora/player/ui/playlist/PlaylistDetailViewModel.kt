package com.sonora.player.ui.playlist

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sonora.player.domain.model.Song
import com.sonora.player.domain.repository.MusicRepository
import com.sonora.player.player.PlayerConnection
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlaylistDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: MusicRepository,
    private val playerConnection: PlayerConnection
) : ViewModel() {

    private val playlistId: Long = checkNotNull(savedStateHandle["playlistId"])

    val songs: StateFlow<List<Song>> = repository.observeSongsInPlaylist(playlistId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun playFrom(song: Song) {
        val list = songs.value
        val index = list.indexOfFirst { it.id == song.id }.coerceAtLeast(0)
        playerConnection.playQueue(list, index)
        viewModelScope.launch { repository.recordPlay(song.id) }
    }

    fun removeSong(songId: Long) {
        viewModelScope.launch { repository.removeSongFromPlaylist(playlistId, songId) }
    }
}
