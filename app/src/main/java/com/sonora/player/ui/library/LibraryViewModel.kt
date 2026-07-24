package com.sonora.player.ui.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sonora.player.domain.model.Song
import com.sonora.player.domain.repository.MusicRepository
import com.sonora.player.player.PlayerConnection
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LibraryUiState(
    val songs: List<Song> = emptyList(),
    val isLoading: Boolean = false,
    val searchQuery: String = ""
)

@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val repository: MusicRepository,
    private val playerConnection: PlayerConnection
) : ViewModel() {

    private val searchQuery = MutableStateFlow("")
    private val isLoading = MutableStateFlow(false)

    val uiState: StateFlow<LibraryUiState> = combine(
        repository.observeLibrary(),
        searchQuery,
        isLoading
    ) { songs, query, loading ->
        val filtered = if (query.isBlank()) {
            songs
        } else {
            songs.filter {
                it.title.contains(query, ignoreCase = true) ||
                    it.artist.contains(query, ignoreCase = true) ||
                    it.album.contains(query, ignoreCase = true)
            }
        }
        LibraryUiState(songs = filtered, isLoading = loading, searchQuery = query)
    }.stateIn(
        scope = viewModelScope,
        started = kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5000),
        initialValue = LibraryUiState()
    )

    init {
        refresh()
        viewModelScope.launch { playerConnection.connect() }
    }

    fun refresh() {
        viewModelScope.launch {
            isLoading.value = true
            repository.refreshLibrary()
            isLoading.value = false
        }
    }

    fun onSearchQueryChange(query: String) {
        searchQuery.value = query
    }

    fun playSong(song: Song) {
        val songs = uiState.value.songs
        val index = songs.indexOfFirst { it.id == song.id }.coerceAtLeast(0)
        playerConnection.playQueue(songs, index)
        viewModelScope.launch { repository.recordPlay(song.id) }
    }

    fun toggleFavorite(songId: Long) {
        viewModelScope.launch { repository.toggleFavorite(songId) }
    }
}
