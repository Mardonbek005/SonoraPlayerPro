package com.sonora.player.domain

import com.sonora.player.domain.model.PlaybackState
import com.sonora.player.domain.model.RepeatMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class PlaybackStateTest {

    @Test
    fun `default playback state is paused with no song`() {
        val state = PlaybackState()
        assertFalse(state.isPlaying)
        assertEquals(null, state.currentSong)
        assertEquals(RepeatMode.OFF, state.repeatMode)
        assertEquals(1.0f, state.playbackSpeed)
    }

    @Test
    fun `default speed is within valid clamp range`() {
        val state = PlaybackState()
        assertTrueInRange(state.playbackSpeed, 0.5f, 2.0f)
    }

    private fun assertTrueInRange(value: Float, min: Float, max: Float) {
        assert(value in min..max) { "$value not in [$min, $max]" }
    }
}
