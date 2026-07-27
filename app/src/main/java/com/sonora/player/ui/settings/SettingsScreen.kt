package com.sonora.player.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun SettingsScreen(onOpenEqualizer: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Text("Sozlamalar", style = MaterialTheme.typography.headlineMedium)

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 24.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(16.dp))
                .clickable(onClick = onOpenEqualizer)
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Filled.GraphicEq, contentDescription = null)
            Text(
                text = "Equalizer",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier
                    .padding(start = 16.dp)
                    .weight(1f)
            )
            Icon(Icons.Filled.ChevronRight, contentDescription = null)
        }

        Text(
            "Sleep Timer, Crossfade va boshqa sozlamalar keyingi bosqichda qo'shiladi.",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(top = 16.dp)
        )
    }
}
