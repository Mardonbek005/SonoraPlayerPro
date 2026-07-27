package com.sonora.player.player.audioeffects

import android.media.audiofx.BassBoost
import android.media.audiofx.Equalizer
import android.media.audiofx.Virtualizer
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

data class EqualizerBand(
    val index: Int,
    val centerFreqHz: Int,
    val minLevelMillibel: Int,
    val maxLevelMillibel: Int,
    val currentLevelMillibel: Int
)

data class EqualizerCapabilities(
    val bands: List<EqualizerBand>,
    val hasBassBoost: Boolean,
    val hasVirtualizer: Boolean,
    val bassBoostStrength: Int, // 0-1000
    val virtualizerStrength: Int // 0-1000
)

/**
 * Thin wrapper around the platform's real AudioEffect APIs.
 *
 * Important: Android does NOT guarantee any particular band count. Most
 * phones expose 5-6 bands (Equalizer.getNumberOfBands()); higher counts
 * exist only on some OEM DSPs. This class always reports the device's
 * actual capabilities — the UI renders however many bands truly exist
 * rather than a hardcoded number, so nothing here is faked.
 */
@Singleton
class EqualizerController @Inject constructor() {

    private var equalizer: Equalizer? = null
    private var bassBoost: BassBoost? = null
    private var virtualizer: Virtualizer? = null
    private var attachedSessionId: Int = -1

    /** (Re)attaches effects to the given ExoPlayer audio session. Safe to call repeatedly. */
    fun attach(audioSessionId: Int) {
        if (audioSessionId == 0 || audioSessionId == attachedSessionId) return
        release()

        runCatching {
            equalizer = Equalizer(0, audioSessionId).apply { enabled = true }
        }.onFailure { Timber.w(it, "Equalizer unavailable on this device") }

        runCatching {
            bassBoost = BassBoost(0, audioSessionId).apply { enabled = true }
        }.onFailure { Timber.w(it, "BassBoost unavailable on this device") }

        runCatching {
            virtualizer = Virtualizer(0, audioSessionId).apply { enabled = true }
        }.onFailure { Timber.w(it, "Virtualizer unavailable on this device") }

        attachedSessionId = audioSessionId
    }

    fun release() {
        equalizer?.release()
        bassBoost?.release()
        virtualizer?.release()
        equalizer = null
        bassBoost = null
        virtualizer = null
        attachedSessionId = -1
    }

    fun setEnabled(enabled: Boolean) {
        equalizer?.enabled = enabled
        bassBoost?.enabled = enabled
        virtualizer?.enabled = enabled
    }

    fun capabilities(): EqualizerCapabilities? {
        val eq = equalizer ?: return null
        val range = eq.bandLevelRange // [min, max] in millibel
        val bands = (0 until eq.numberOfBands).map { i ->
            val bandIndex = i.toShort()
            EqualizerBand(
                index = i,
                centerFreqHz = eq.getCenterFreq(bandIndex) / 1000, // milliHz -> Hz
                minLevelMillibel = range[0].toInt(),
                maxLevelMillibel = range[1].toInt(),
                currentLevelMillibel = eq.getBandLevel(bandIndex).toInt()
            )
        }
        return EqualizerCapabilities(
            bands = bands,
            hasBassBoost = bassBoost != null,
            hasVirtualizer = virtualizer != null,
            bassBoostStrength = runCatching { bassBoost?.roundedStrength?.toInt() }.getOrNull() ?: 0,
            virtualizerStrength = runCatching { virtualizer?.roundedStrength?.toInt() }.getOrNull() ?: 0
        )
    }

    fun setBandLevel(bandIndex: Int, levelMillibel: Int) {
        runCatching { equalizer?.setBandLevel(bandIndex.toShort(), levelMillibel.toShort()) }
    }

    fun setBassBoostStrength(strength: Int) {
        runCatching { bassBoost?.setStrength(strength.toShort()) }
    }

    fun setVirtualizerStrength(strength: Int) {
        runCatching { virtualizer?.setStrength(strength.toShort()) }
    }

    /** Applies a named preset if the device's built-in Equalizer exposes one matching by name. */
    fun applyDevicePreset(presetName: String): Boolean {
        val eq = equalizer ?: return false
        for (i in 0 until eq.numberOfPresets) {
            if (eq.getPresetName(i.toShort()).equals(presetName, ignoreCase = true)) {
                eq.usePreset(i.toShort())
                return true
            }
        }
        return false
    }
}
