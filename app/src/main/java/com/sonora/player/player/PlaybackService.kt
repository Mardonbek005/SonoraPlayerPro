package com.sonora.player.player

import android.app.PendingIntent
import android.content.Intent
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.sonora.player.MainActivity
import dagger.hilt.android.AndroidEntryPoint

/**
 * Foreground service hosting the ExoPlayer instance + MediaSession.
 *
 * Using Media3's MediaSessionService gets us, essentially for free:
 * - Lock screen controls
 * - Notification controls (play/pause/next/prev)
 * - Bluetooth / wired headset button handling
 * - Proper audio focus handling (ducking, pausing on call, etc.)
 * - Android Auto exposure (the same MediaSession is what Auto binds to)
 *
 * The app's ViewModels talk to this service through a MediaController
 * (see player/PlayerConnection.kt), never directly — that keeps playback
 * state as a single source of truth owned by the service, which is what
 * lets playback survive the UI being destroyed/recreated.
 */
@AndroidEntryPoint
class PlaybackService : MediaSessionService() {

    private var mediaSession: MediaSession? = null

    override fun onCreate() {
        super.onCreate()

        val audioAttributes = AudioAttributes.Builder()
            .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
            .setUsage(C.USAGE_MEDIA)
            .build()

        val player = ExoPlayer.Builder(this)
            .setAudioAttributes(audioAttributes, /* handleAudioFocus = */ true)
            .setHandleAudioBecomingNoisy(true)
            .build()

        val sessionActivityIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        mediaSession = MediaSession.Builder(this, player)
            .setSessionActivity(sessionActivityIntent)
            .build()
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? = mediaSession

    override fun onDestroy() {
        mediaSession?.run {
            player.release()
            release()
            mediaSession = null
        }
        super.onDestroy()
    }
}
