package com.sonora.player.player

import android.content.ComponentName
import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.sonora.player.domain.model.PlaybackState
import com.sonora.player.domain.model.RepeatMode
import com.sonora.player.domain.model.Song
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.guava.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Single connection point between the app's UI/ViewModels and the
 * MediaController that talks to PlaybackService. All playback commands
 * (play, pause, seek, skip, shuffle, repeat, queue) go through here.
 */
@Singleton
class PlayerConnection @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private var controller: MediaController? = null
    private var currentQueue: List<Song> = emptyList()

    private val _state = MutableStateFlow(PlaybackState())
    val state: StateFlow<PlaybackState> = _state.asStateFlow()

    private val playerListener = object : Player.Listener {
        override fun onIsPlayingChanged(isPlaying: Boolean) {
            _state.value = _state.value.copy(isPlaying = isPlaying)
        }

        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
            val index = controller?.currentMediaItemIndex ?: -1
            val song = currentQueue.getOrNull(index)
            _state.value = _state.value.copy(currentSong = song, currentIndex = index)
        }

        override fun onPositionDiscontinuity(oldPosition: Player.PositionInfo, newPosition: Player.PositionInfo, reason: Int) {
            _state.value = _state.value.copy(positionMs = newPosition.positionMs)
        }

        override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {
            _state.value = _state.value.copy(shuffleEnabled = shuffleModeEnabled)
        }

        override fun onRepeatModeChanged(repeatMode: Int) {
            _state.value = _state.value.copy(repeatMode = repeatMode.toDomainRepeatMode())
        }

        override fun onPlaybackParametersChanged(playbackParameters: androidx.media3.common.PlaybackParameters) {
            _state.value = _state.value.copy(playbackSpeed = playbackParameters.speed)
        }
    }

    suspend fun connect() {
        if (controller != null) return
        val sessionToken = SessionToken(context, ComponentName(context, PlaybackService::class.java))
        controller = MediaController.Builder(context, sessionToken).buildAsync().await().also {
            it.addListener(playerListener)
        }
    }

    fun disconnect() {
        controller?.removeListener(playerListener)
        controller?.release()
        controller = null
    }

    /** Loads a new queue and starts playback at [startIndex]. */
    fun playQueue(songs: List<Song>, startIndex: Int) {
        val player = controller ?: return
        currentQueue = songs
        val mediaItems = songs.map { it.toMediaItem() }
        player.setMediaItems(mediaItems, startIndex, 0L)
        player.prepare()
        player.play()
        _state.value = _state.value.copy(queue = songs, currentIndex = startIndex, currentSong = songs.getOrNull(startIndex))
    }

    fun togglePlayPause() {
        val player = controller ?: return
        if (player.isPlaying) player.pause() else player.play()
    }

    fun skipToNext() = controller?.seekToNextMediaItem()
    fun skipToPrevious() = controller?.seekToPreviousMediaItem()
    fun seekTo(positionMs: Long) = controller?.seekTo(positionMs)

    fun setShuffleEnabled(enabled: Boolean) {
        controller?.shuffleModeEnabled = enabled
    }

    fun cycleRepeatMode() {
        val player = controller ?: return
        player.repeatMode = when (player.repeatMode) {
            Player.REPEAT_MODE_OFF -> Player.REPEAT_MODE_ALL
            Player.REPEAT_MODE_ALL -> Player.REPEAT_MODE_ONE
            else -> Player.REPEAT_MODE_OFF
        }
    }

    fun setPlaybackSpeed(speed: Float) {
        controller?.setPlaybackSpeed(speed.coerceIn(0.5f, 2.0f))
    }

    /**
     * Used as the Equalizer screen's "Preamp" knob. Note: this is an
     * attenuator (0f..1f), not a true gain boost above unity — Media3's
     * standard Player.setVolume() doesn't amplify beyond the source signal.
     * A real >0dB preamp would need a custom AudioProcessor; that's flagged
     * as future work rather than faked here.
     */
    fun setVolume(volume: Float) {
        controller?.volume = volume.coerceIn(0f, 1f)
    }

    fun currentVolume(): Float = controller?.volume ?: 1f

    fun currentPositionMs(): Long = controller?.currentPosition ?: 0L
}

private fun Song.toMediaItem(): MediaItem =
    MediaItem.Builder()
        .setUri(contentUri)
        .setMediaId(id.toString())
        .setMediaMetadata(
            MediaMetadata.Builder()
                .setTitle(title)
                .setArtist(artist)
                .setAlbumTitle(album)
                .setArtworkUri(albumArtUri?.let { android.net.Uri.parse(it) })
                .build()
        )
        .build()

private fun Int.toDomainRepeatMode(): RepeatMode = when (this) {
    Player.REPEAT_MODE_ONE -> RepeatMode.ONE
    Player.REPEAT_MODE_ALL -> RepeatMode.ALL
    else -> RepeatMode.OFF
}
