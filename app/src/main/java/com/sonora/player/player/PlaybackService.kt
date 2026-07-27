package com.sonora.player.player

import android.app.PendingIntent
import android.content.Intent
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.sonora.player.MainActivity
import com.sonora.player.player.audioeffects.EqualizerController
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

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
 *
 * The Equalizer/BassBoost/Virtualizer effects are attached HERE rather than
 * from the UI side: AudioEffect needs a real audio session id, and only the
 * actual ExoPlayer instance (not a client-side MediaController) exposes
 * one. EqualizerController is a Hilt @Singleton, so the same instance is
 * shared with EqualizerViewModel since this service runs in the app's main
 * process.
 */
@AndroidEntryPoint
class PlaybackService : MediaSessionService() {

    private var mediaSession: MediaSession? = null
    private var exoPlayer: ExoPlayer? = null

    @Inject
    lateinit var equalizerController: EqualizerController

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
        exoPlayer = player

        player.addListener(object : Player.Listener {
            override fun onMediaItemTransition(mediaItem: androidx.media3.common.MediaItem?, reason: Int) {
                attachEqualizer(player)
            }
        })
        attachEqualizer(player)

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

    private fun attachEqualizer(player: ExoPlayer) {
        val sessionId = player.audioSessionId
        if (sessionId != 0) {
            equalizerController.attach(sessionId)
        }
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? = mediaSession

    override fun onDestroy() {
        equalizerController.release()
        mediaSession?.run {
            player.release()
            release()
            mediaSession = null
        }
        exoPlayer = null
        super.onDestroy()
    }
}
