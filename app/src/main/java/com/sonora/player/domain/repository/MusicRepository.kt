package com.sonora.player.domain.repository

import com.sonora.player.domain.model.Playlist
import com.sonora.player.domain.model.Song
import kotlinx.coroutines.flow.Flow

interface MusicRepository {
    suspend fun refreshLibrary(): List<Song>
    fun observeLibrary(): Flow<List<Song>>
    suspend fun getSongById(id: Long): Song?

    fun observePlaylists(): Flow<List<Playlist>>
    suspend fun createPlaylist(name: String): Long
    suspend fun deletePlaylist(playlistId: Long)
    suspend fun addSongToPlaylist(playlistId: Long, songId: Long)
    suspend fun removeSongFromPlaylist(playlistId: Long, songId: Long)
    fun observeSongsInPlaylist(playlistId: Long): Flow<List<Song>>

    fun observeIsFavorite(songId: Long): Flow<Boolean>
    fun observeFavoriteSongs(): Flow<List<Song>>
    suspend fun toggleFavorite(songId: Long)

    suspend fun recordPlay(songId: Long)
    fun observeRecentlyPlayed(): Flow<List<Song>>
    fun observeMostPlayed(): Flow<List<Song>>
}
