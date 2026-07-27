package com.sonora.player.ui.equalizer

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun EqualizerScreen(
    viewModel: EqualizerViewModel,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Pro Equalizer",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier, Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            KnobControl(title = "Bass Boost")
            KnobControl(title = "3D Virtual")
        }

        Spacer(modifier = Modifier.height(32.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            val frequencies = listOf("60Hz", "230Hz", "910Hz", "3.6kHz", "14kHz")
            frequencies.forEach { freq ->
                VerticalSliderBand(frequency = freq)
            }
        }
    }
}

@Composable
fun KnobControl(title: String) {
    var value by remember { mutableFloatStateOf(0f) }
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Slider(
            value = value,
            onValueChange = { value = it },
            modifier = Modifier.width(100.dp)
        )
        Text(text = title, style = MaterialTheme.typography.labelMedium)
    }
}

@Composable
fun VerticalSliderBand(frequency: String) {
    var sliderValue by remember { mutableFloatStateOf(0f) }
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(horizontal = 4.dp)
    ) {
        Text(text = "+15", style = MaterialTheme.typography.labelSmall)
        Slider(
            value = sliderValue,
            onValueChange = { sliderValue = it },
            valueRange = -15f..15f,
            modifier = Modifier
                .weight(1f)
                .padding(vertical = 8.dp)
        )
        Text(text = "-15", style = MaterialTheme.typography.labelSmall)
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = frequency, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
    }
}
