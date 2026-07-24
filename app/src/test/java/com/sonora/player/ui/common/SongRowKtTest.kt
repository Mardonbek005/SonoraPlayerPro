package com.sonora.player.ui.common

import org.junit.Assert.assertEquals
import org.junit.Test

class SongRowKtTest {

    @Test
    fun `formats sub-minute duration correctly`() {
        assertEquals("0:45", formatDuration(45_000))
    }

    @Test
    fun `formats multi-minute duration correctly`() {
        assertEquals("3:05", formatDuration(185_000))
    }

    @Test
    fun `formats zero duration correctly`() {
        assertEquals("0:00", formatDuration(0))
    }

    @Test
    fun `pads seconds under ten`() {
        assertEquals("1:09", formatDuration(69_000))
    }
}
