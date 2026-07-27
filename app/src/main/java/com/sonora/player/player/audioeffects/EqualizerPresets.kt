package com.sonora.player.player.audioeffects

import kotlin.math.roundToInt

/**
 * Each preset is a normalized curve (-1f = min gain, 0f = flat, 1f = max
 * gain) sampled at 10 conceptual points spanning low-to-high frequency.
 * These get resampled onto the device's *actual* band count in
 * [applyPreset], so a 5-band phone and a 16-band phone both get a
 * musically sensible approximation of the same curve.
 */
enum class EqualizerPreset(val label: String, val curve: List<Float>) {
    MANUAL("Manual", emptyList()),
    ROCK("Rock", listOf(0.6f, 0.4f, 0.1f, -0.2f, -0.1f, 0.1f, 0.3f, 0.5f, 0.5f, 0.4f)),
    POP("Pop", listOf(-0.1f, 0.1f, 0.3f, 0.4f, 0.3f, 0.0f, -0.1f, -0.1f, 0.1f, 0.2f)),
    JAZZ("Jazz", listOf(0.3f, 0.2f, 0.0f, 0.1f, -0.1f, -0.1f, 0.0f, 0.2f, 0.3f, 0.3f)),
    CLASSICAL("Classical", listOf(0.4f, 0.3f, 0.2f, 0.1f, 0.0f, 0.0f, -0.1f, -0.1f, 0.2f, 0.4f)),
    HIP_HOP("Hip Hop", listOf(0.7f, 0.6f, 0.2f, 0.1f, -0.1f, 0.0f, 0.1f, 0.0f, 0.2f, 0.3f)),
    ELECTRONIC("Electronic", listOf(0.5f, 0.4f, 0.1f, 0.0f, -0.2f, 0.0f, 0.2f, 0.3f, 0.4f, 0.5f))
}

object EqualizerPresetApplier {

    fun applyPreset(controller: EqualizerController, preset: EqualizerPreset) {
        if (preset == EqualizerPreset.MANUAL) return
        val caps = controller.capabilities() ?: return
        val bandCount = caps.bands.size
        if (bandCount == 0) return

        for (band in caps.bands) {
            val positionFraction = if (bandCount == 1) 0f else band.index.toFloat() / (bandCount - 1)
            val curveValue = sampleCurve(preset.curve, positionFraction)
            val range = if (curveValue >= 0f) {
                band.maxLevelMillibel
            } else {
                -band.minLevelMillibel // minLevel is typically negative already
            }
            val level = (curveValue * range).roundToInt()
                .coerceIn(band.minLevelMillibel, band.maxLevelMillibel)
            controller.setBandLevel(band.index, level)
        }
    }

    /** Linearly interpolates a value from the 10-point curve at [fraction] (0f..1f). */
    private fun sampleCurve(curve: List<Float>, fraction: Float): Float {
        if (curve.isEmpty()) return 0f
        val position = fraction * (curve.size - 1)
        val lowerIndex = position.toInt().coerceIn(0, curve.size - 1)
        val upperIndex = (lowerIndex + 1).coerceAtMost(curve.size - 1)
        val t = position - lowerIndex
        return curve[lowerIndex] * (1 - t) + curve[upperIndex] * t
    }
}
