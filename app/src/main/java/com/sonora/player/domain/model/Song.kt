package com.sonora.player.domain.model

/**
 * Immutable domain representation of a single audio track, scanned from the
 * device's MediaStore. This is what the UI and player layers work with —
 * database entities are mapped to/from this at the repository boundary.
 */
data class Song(
    val id: Long,
    val title: String,
    val artist: String,
    val album: String,
    val albumId: Long,
    val genre: String?,
    val durationMs: Long,
    val contentUri: String,
    val albumArtUri: String?,
    val dateAdded: Long,
    val trackNumber: Int = 0
)

enum class RepeatMode { OFF, ONE, ALL }

data class PlaybackState(
    val currentSong: Song? = null,
    val queue: List<Song> = emptyList(),
    val currentIndex: Int = -1,
    val isPlaying: Boolean = false,
    val positionMs: Long = 0L,
    val shuffleEnabled: Boolean = false,
    val repeatMode: RepeatMode = RepeatMode.OFF,
    val playbackSpeed: Float = 1.0f
)
