package com.sonora.player.data.repository

import com.sonora.player.data.database.FavoriteDao
import com.sonora.player.data.database.FavoriteEntity
import com.sonora.player.data.database.HistoryDao
import com.sonora.player.data.database.PlayHistoryEntity
import com.sonora.player.data.database.PlaylistDao
import com.sonora.player.data.database.PlaylistEntity
import com.sonora.player.data.database.PlaylistSongCrossRef
import com.sonora.player.domain.model.Playlist
import com.sonora.player.domain.model.Song
import com.sonora.player.domain.repository.MusicRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MusicRepositoryImpl @Inject constructor(
    private val mediaScanner: MediaScanner,
    private val playlistDao: PlaylistDao,
    private val favoriteDao: FavoriteDao,
    private val historyDao: HistoryDao
) : MusicRepository {

    // In-memory cache of the last scan; refreshed explicitly (pull-to-refresh,
    // app start) rather than on every read, since a full MediaStore query is
    // relatively expensive.
    private val libraryCache = MutableStateFlow<List<Song>>(emptyList())

    override suspend fun refreshLibrary(): List<Song> {
        val songs = mediaScanner.scanAllSongs()
        libraryCache.value = songs
        return songs
    }

    override fun observeLibrary() = libraryCache

    override suspend fun getSongById(id: Long): Song? =
        libraryCache.value.firstOrNull { it.id == id }

    override fun observePlaylists() =
        playlistDao.observePlaylists().map { entities ->
            entities.map { entity ->
                Playlist(
                    id = entity.id,
                    name = entity.name,
                    createdAt = entity.createdAt,
                    songCount = playlistDao.songCountInPlaylist(entity.id)
                )
            }
        }

    override suspend fun createPlaylist(name: String): Long =
        playlistDao.insertPlaylist(PlaylistEntity(name = name, createdAt = System.currentTimeMillis()))

    override suspend fun deletePlaylist(playlistId: Long) {
        playlistDao.clearPlaylistSongs(playlistId)
        playlistDao.deletePlaylist(playlistId)
    }

    override suspend fun addSongToPlaylist(playlistId: Long, songId: Long) {
        val nextPosition = (playlistDao.maxPositionInPlaylist(playlistId) ?: -1) + 1
        playlistDao.addSongToPlaylist(
            PlaylistSongCrossRef(
                playlistId = playlistId,
                songId = songId,
                position = nextPosition,
                addedAt = System.currentTimeMillis()
            )
        )
    }

    override suspend fun removeSongFromPlaylist(playlistId: Long, songId: Long) {
        playlistDao.removeSongFromPlaylist(playlistId, songId)
    }

    override fun observeSongsInPlaylist(playlistId: Long) =
        combine(playlistDao.observeSongIdsInPlaylist(playlistId), libraryCache) { ids, library ->
            val byId = library.associateBy { it.id }
            ids.mapNotNull { byId[it] }
        }

    override fun observeIsFavorite(songId: Long) = favoriteDao.observeIsFavorite(songId)

    override fun observeFavoriteSongs() =
        combine(favoriteDao.observeFavoriteSongIds(), libraryCache) { ids, library ->
            val byId = library.associateBy { it.id }
            ids.mapNotNull { byId[it] }
        }

    override suspend fun toggleFavorite(songId: Long) {
        val currentlyFavorite = favoriteDao.observeIsFavorite(songId).first()
        if (currentlyFavorite) {
            favoriteDao.removeFavorite(songId)
        } else {
            favoriteDao.addFavorite(FavoriteEntity(songId, System.currentTimeMillis()))
        }
    }

    override suspend fun recordPlay(songId: Long) {
        val now = System.currentTimeMillis()
        historyDao.recordPlay(PlayHistoryEntity(songId = songId, playedAt = now))
        historyDao.incrementPlayCount(songId, now)
    }

    override fun observeRecentlyPlayed() =
        combine(historyDao.observeRecentlyPlayedIds(), libraryCache) { ids, library ->
            val byId = library.associateBy { it.id }
            ids.mapNotNull { byId[it] }
        }

    override fun observeMostPlayed() =
        combine(historyDao.observeMostPlayedIds(), libraryCache) { ids, library ->
            val byId = library.associateBy { it.id }
            ids.mapNotNull { byId[it] }
        }
}
