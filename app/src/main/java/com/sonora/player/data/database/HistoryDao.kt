package com.sonora.player.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface HistoryDao {

    @Insert
    suspend fun recordPlay(entry: PlayHistoryEntity)

    @Query(
        """
        SELECT DISTINCT songId FROM play_history
        ORDER BY playedAt DESC
        LIMIT :limit
        """
    )
    fun observeRecentlyPlayedIds(limit: Int = 50): Flow<List<Long>>

    @Query("SELECT * FROM play_counts WHERE songId = :songId")
    suspend fun getPlayCount(songId: Long): PlayCountEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertPlayCount(entry: PlayCountEntity)

    @Query("SELECT songId FROM play_counts ORDER BY playCount DESC LIMIT :limit")
    fun observeMostPlayedIds(limit: Int = 50): Flow<List<Long>>

    suspend fun incrementPlayCount(songId: Long, timestamp: Long) {
        val existing = getPlayCount(songId)
        upsertPlayCount(
            PlayCountEntity(
                songId = songId,
                playCount = (existing?.playCount ?: 0) + 1,
                lastPlayedAt = timestamp
            )
        )
    }
}
