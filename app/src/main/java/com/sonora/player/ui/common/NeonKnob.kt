package com.sonora.player.ui.common

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * A rotary knob controlled by vertical drag (drag up = increase, drag down
 * = decrease), matching the reference design's Preamp / Bass Boost /
 * Virtualizer dials. Renders a glowing arc indicator around the dial
 * showing the current value within [valueRange].
 */
@Composable
fun NeonKnob(
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float>,
    label: String,
    valueLabel: String,
    accentColor: Color,
    modifier: Modifier = Modifier,
    knobSize: Dp = 88.dp
) {
    val fraction = ((value - valueRange.start) / (valueRange.endInclusive - valueRange.start)).coerceIn(0f, 1f)
    val sweepAngle = 270f * fraction
    val startAngle = 135f

    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(knobSize)
                .pointerInput(valueRange) {
                    detectDragGestures(
                        onDrag = { change, dragAmount ->
                            change.consume()
                            val range = valueRange.endInclusive - valueRange.start
                            // A ~300px drag sweeps the full range.
                            val delta = (-dragAmount.y / 300f) * range
                            val newValue = (value + delta).coerceIn(valueRange.start, valueRange.endInclusive)
                            onValueChange(newValue)
                        }
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.size(knobSize)) {
                val strokeWidth = 6.dp.toPx()
                val inset = strokeWidth
                val arcSize = Size(this.size.width - inset * 2, this.size.height - inset * 2)
                val topLeft = Offset(inset, inset)

                drawArc(
                    color = Color.White.copy(alpha = 0.08f),
                    startAngle = startAngle,
                    sweepAngle = 270f,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
                drawArc(
                    brush = Brush.sweepGradient(listOf(accentColor.copy(alpha = 0.4f), accentColor)),
                    startAngle = startAngle,
                    sweepAngle = sweepAngle,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
            }

            Box(
                modifier = Modifier
                    .size(knobSize - 24.dp)
                    .background(Color(0xFF15131C), CircleShape)
            )

            Box(
                modifier = Modifier
                    .size(knobSize - 24.dp)
                    .rotate(startAngle + sweepAngle + 90f),
                contentAlignment = Alignment.TopCenter
            ) {
                Box(
                    modifier = Modifier
                        .size(width = 3.dp, height = (knobSize.value * 0.28f).dp)
                        .background(Color.White, RoundedCornerShape(2.dp))
                )
            }
        }

        Text(text = label, color = Color.White.copy(alpha = 0.7f), style = MaterialTheme.typography.labelSmall)
        Text(text = valueLabel, color = Color.White, style = MaterialTheme.typography.bodyMedium)
    }
}
