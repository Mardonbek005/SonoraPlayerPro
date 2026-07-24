package com.sonora.player.data.repository

import android.content.ContentUris
import android.content.Context
import android.provider.MediaStore
import com.sonora.player.domain.model.Song
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Scans the device's MediaStore for audio tracks. This is the single source
 * of truth for "what songs exist" — we deliberately do NOT duplicate this
 * into Room; only user-generated data (playlists, favorites, history)
 * lives in the database and references songs by their stable MediaStore id.
 */
@Singleton
class MediaScanner @Inject constructor(
    @ApplicationContext private val context: Context
) {

    suspend fun scanAllSongs(): List<Song> = withContext(Dispatchers.IO) {
        val songs = mutableListOf<Song>()

        val collection = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.GENRE,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.DATE_ADDED,
            MediaStore.Audio.Media.TRACK,
            MediaStore.Audio.Media.IS_MUSIC
        )
        val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0 AND ${MediaStore.Audio.Media.DURATION} >= 15000"
        val sortOrder = "${MediaStore.Audio.Media.TITLE} ASC"

        context.contentResolver.query(collection, projection, selection, null, sortOrder)?.use { cursor ->
            val idCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val titleCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
            val artistCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
            val albumCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
            val albumIdCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)
            val genreCol = cursor.getColumnIndex(MediaStore.Audio.Media.GENRE)
            val durationCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
            val dateAddedCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_ADDED)
            val trackCol = cursor.getColumnIndex(MediaStore.Audio.Media.TRACK)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idCol)
                val albumId = cursor.getLong(albumIdCol)
                val contentUri = ContentUris.withAppendedId(collection, id)
                val albumArtUri = ContentUris.withAppendedId(
                    android.net.Uri.parse("content://media/external/audio/albumart"),
                    albumId
                )

                songs += Song(
                    id = id,
                    title = cursor.getString(titleCol) ?: "Noma'lum",
                    artist = cursor.getString(artistCol) ?: "Noma'lum ijrochi",
                    album = cursor.getString(albumCol) ?: "Noma'lum albom",
                    albumId = albumId,
                    genre = if (genreCol >= 0) cursor.getString(genreCol) else null,
                    durationMs = cursor.getLong(durationCol),
                    contentUri = contentUri.toString(),
                    albumArtUri = albumArtUri.toString(),
                    dateAdded = cursor.getLong(dateAddedCol),
                    trackNumber = if (trackCol >= 0) cursor.getInt(trackCol) % 1000 else 0
                )
            }
        }

        songs
    }
}
