package com.sonora.player.ui.favorites

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
class FavoritesViewModel @Inject constructor(
    private val repository: MusicRepository,
    private val playerConnection: PlayerConnection
) : ViewModel() {

    val favorites: StateFlow<List<Song>> = repository.observeFavoriteSongs()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun playFrom(song: Song) {
        val list = favorites.value
        val index = list.indexOfFirst { it.id == song.id }.coerceAtLeast(0)
        playerConnection.playQueue(list, index)
        viewModelScope.launch { repository.recordPlay(song.id) }
    }

    fun toggleFavorite(songId: Long) {
        viewModelScope.launch { repository.toggleFavorite(songId) }
    }
}
