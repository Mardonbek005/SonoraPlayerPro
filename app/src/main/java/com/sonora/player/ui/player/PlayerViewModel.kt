package com.sonora.player.ui.player

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sonora.player.domain.model.PlaybackState
import com.sonora.player.domain.repository.MusicRepository
import com.sonora.player.player.PlayerConnection
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlayerViewModel @Inject constructor(
    private val playerConnection: PlayerConnection,
    private val repository: MusicRepository
) : ViewModel() {

    private val _playbackState = MutableStateFlow(PlaybackState())
    val playbackState: StateFlow<PlaybackState> = _playbackState.asStateFlow()

    init {
        // Mirror every state change from the connection (song changes,
        // play/pause, shuffle/repeat, speed) immediately.
        viewModelScope.launch {
            playerConnection.state.collect { connectionState ->
                _playbackState.value = connectionState.copy(positionMs = _playbackState.value.positionMs)
            }
        }
        // Separately sample the live playback position every 500ms so the
        // seek bar advances smoothly while a track is playing, without
        // needing a callback for every single position change.
        viewModelScope.launch {
            while (true) {
                delay(500)
                if (_playbackState.value.isPlaying) {
                    _playbackState.value = _playbackState.value.copy(
                        positionMs = playerConnection.currentPositionMs()
                    )
                }
            }
        }
    }

    fun togglePlayPause() = playerConnection.togglePlayPause()
    fun skipToNext() = playerConnection.skipToNext()
    fun skipToPrevious() = playerConnection.skipToPrevious()
    fun seekTo(positionMs: Long) = playerConnection.seekTo(positionMs)
    fun toggleShuffle() = playerConnection.setShuffleEnabled(!playbackState.value.shuffleEnabled)
    fun cycleRepeatMode() = playerConnection.cycleRepeatMode()
    fun setPlaybackSpeed(speed: Float) = playerConnection.setPlaybackSpeed(speed)

    fun toggleFavoriteForCurrentSong() {
        val songId = playbackState.value.currentSong?.id ?: return
        viewModelScope.launch { repository.toggleFavorite(songId) }
    }
}
