package com.sonora.player.di

import android.content.Context
import androidx.room.Room
import com.sonora.player.data.database.FavoriteDao
import com.sonora.player.data.database.HistoryDao
import com.sonora.player.data.database.PlaylistDao
import com.sonora.player.data.database.SonoraDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): SonoraDatabase =
        Room.databaseBuilder(context, SonoraDatabase::class.java, SonoraDatabase.DATABASE_NAME)
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    fun providePlaylistDao(db: SonoraDatabase): PlaylistDao = db.playlistDao()

    @Provides
    fun provideFavoriteDao(db: SonoraDatabase): FavoriteDao = db.favoriteDao()

    @Provides
    fun provideHistoryDao(db: SonoraDatabase): HistoryDao = db.historyDao()
}
