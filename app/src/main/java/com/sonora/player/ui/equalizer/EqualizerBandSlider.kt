package com.sonora.player.ui.equalizer

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.sonora.player.player.audioeffects.EqualizerBand

@Composable
fun EqualizerBandSlider(
    band: EqualizerBand,
    onLevelChange: (Int) -> Unit,
    accentColor: Color,
    trackHeight: androidx.compose.ui.unit.Dp = 220.dp
) {
    val range = (band.maxLevelMillibel - band.minLevelMillibel).coerceAtLeast(1)
    val fraction = (band.currentLevelMillibel - band.minLevelMillibel).toFloat() / range
    val gainDb = band.currentLevelMillibel / 100f

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "%.1f".format(gainDb),
            color = if (gainDb >= 0) accentColor else Color(0xFF8B8FA3),
            style = MaterialTheme.typography.labelSmall
        )

        Box(
            modifier = Modifier
                .padding(top = 6.dp)
                .width(40.dp)
                .height(trackHeight)
                .pointerInput(band.index) {
                    detectVerticalDragGestures { change, _ ->
                        change.consume()
                        val boxHeightPx = size.height.toFloat()
                        val relativeY = (change.position.y / boxHeightPx).coerceIn(0f, 1f)
                        // Top of the track = max gain, bottom = min gain.
                        val newFraction = 1f - relativeY
                        val newLevel = (band.minLevelMillibel + newFraction * range).toInt()
                        onLevelChange(newLevel.coerceIn(band.minLevelMillibel, band.maxLevelMillibel))
                    }
                },
            contentAlignment = Alignment.BottomCenter
        ) {
            // Track line
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .height(trackHeight)
                    .align(Alignment.Center)
                    .background(Color.White.copy(alpha = 0.12f), RoundedCornerShape(2.dp))
            )
            // Filled portion from center(0dB) to thumb, echoing the reference's gradient fill
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .height(trackHeight * fraction)
                    .align(Alignment.BottomCenter)
                    .background(
                        Brush.verticalGradient(listOf(accentColor, accentColor.copy(alpha = 0.4f))),
                        RoundedCornerShape(2.dp)
                    )
            )
            // Thumb
            Box(
                modifier = Modifier
                    .padding(bottom = (trackHeight * fraction) - 9.dp)
                    .size(18.dp)
                    .shadow(elevation = 6.dp, shape = CircleShape, ambientColor = accentColor, spotColor = accentColor)
                    .background(Color.White, CircleShape)
                    .background(accentColor.copy(alpha = 0.25f), CircleShape)
            )
        }

        Text(
            text = formatFreqLabel(band.centerFreqHz),
            color = Color.White.copy(alpha = 0.6f),
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(top = 6.dp)
        )
    }
}

private fun formatFreqLabel(hz: Int): String =
    if (hz >= 1000) "${(hz / 1000f).let { if (it % 1f == 0f) it.toInt().toString() else "%.1f".format(it) }}K" else "$hz"
