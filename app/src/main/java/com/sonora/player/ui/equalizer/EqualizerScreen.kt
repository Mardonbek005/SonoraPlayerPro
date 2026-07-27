package com.sonora.player.ui.equalizer

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.sonora.player.player.audioeffects.EqualizerPreset

private val NeonViolet = Color(0xFFA855F7)
private val NeonViolet2 = Color(0xFF6D5EF5)
private val PanelBackground = Color(0xFF13111A)
private val PanelBorder = Color(0x33A855F7)

@Composable
fun EqualizerScreen(
    onBack: () -> Unit,
    viewModel: EqualizerViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0B0A10))
            .padding(16.dp)
    ) {
        EqualizerHeader(
            bandCount = state.capabilities?.bands?.size ?: 0,
            enabled = state.enabled,
            onEnabledChange = viewModel::setEnabled,
            onBack = onBack
        )

        Spacer(modifier = Modifier.height(20.dp))

        if (!state.isSupported) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = "Equalizer effekti hozircha mavjud emas.\nAvval biror qo'shiqni ijro eting.",
                    color = Color.White.copy(alpha = 0.7f),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            return
        }

        // Bands panel
        GlassPanel {
            val bands = state.capabilities?.bands.orEmpty()
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(vertical = 16.dp, horizontal = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                bands.forEach { band ->
                    EqualizerBandSlider(
                        band = band,
                        onLevelChange = { newLevel -> viewModel.setBandLevel(band.index, newLevel) },
                        accentColor = NeonViolet
                    )
                }
            }
            Text(
                text = "${bands.size} BAND",
                color = NeonViolet,
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier
                    .align(Alignment.End)
                    .padding(top = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Presets row
        GlassPanel {
            Text("PRESETLAR", color = Color.White.copy(alpha = 0.6f), style = MaterialTheme.typography.labelSmall)
            Spacer(modifier = Modifier.height(10.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                items(EqualizerPreset.values().toList()) { preset ->
                    PresetChip(
                        label = preset.label,
                        selected = state.selectedPreset == preset,
                        onClick = { viewModel.selectPreset(preset) }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Knobs panel
        GlassPanel {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                com.sonora.player.ui.common.NeonKnob(
                    value = state.preampDb,
                    onValueChange = viewModel::setPreamp,
                    valueRange = 0f..100f,
                    label = "Preamp",
                    valueLabel = "${state.preampDb.toInt()}%",
                    accentColor = NeonViolet2
                )
                com.sonora.player.ui.common.NeonKnob(
                    value = state.bassBoostPercent,
                    onValueChange = viewModel::setBassBoost,
                    valueRange = 0f..100f,
                    label = "Bass Boost",
                    valueLabel = "${state.bassBoostPercent.toInt()}%",
                    accentColor = NeonViolet
                )
                com.sonora.player.ui.common.NeonKnob(
                    value = state.virtualizerPercent,
                    onValueChange = viewModel::setVirtualizer,
                    valueRange = 0f..100f,
                    label = "Virtualizer",
                    valueLabel = "${state.virtualizerPercent.toInt()}%",
                    accentColor = NeonViolet2
                )
            }
        }
    }
}

@Composable
private fun EqualizerHeader(
    bandCount: Int,
    enabled: Boolean,
    onEnabledChange: (Boolean) -> Unit,
    onBack: () -> Unit
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = onBack) {
            Icon(Icons.Filled.ArrowBackIosNew, contentDescription = null, tint = Color.White)
        }
        Icon(
            imageVector = Icons.Filled.GraphicEq,
            contentDescription = null,
            tint = NeonViolet,
            modifier = Modifier
                .size(22.dp)
                .padding(end = 4.dp)
        )
        Text("Equalizer", color = Color.White, style = MaterialTheme.typography.titleMedium)
        if (bandCount > 0) {
            Spacer(modifier = Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .background(NeonViolet.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                    .border(1.dp, NeonViolet.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text("$bandCount BAND", color = NeonViolet, style = MaterialTheme.typography.labelSmall)
            }
        }
        Spacer(modifier = Modifier.weight(1f))
        Switch(
            checked = enabled,
            onCheckedChange = onEnabledChange,
            colors = SwitchDefaults.colors(checkedTrackColor = NeonViolet, checkedThumbColor = Color.White)
        )
    }
}

@Composable
private fun GlassPanel(content: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(PanelBackground, RoundedCornerShape(24.dp))
            .border(1.dp, PanelBorder, RoundedCornerShape(24.dp))
            .padding(16.dp),
        content = content
    )
}

@Composable
private fun PresetChip(label: String, selected: Boolean, onClick: () -> Unit) {
    val background = if (selected) {
        Brush.horizontalGradient(listOf(NeonViolet2, NeonViolet))
    } else {
        Brush.horizontalGradient(listOf(Color(0xFF1E1B26), Color(0xFF1E1B26)))
    }
    Box(
        modifier = Modifier
            .background(background, RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        Text(
            text = label,
            color = if (selected) Color.White else Color.White.copy(alpha = 0.65f),
            style = MaterialTheme.typography.bodyMedium
        )
    }
}
