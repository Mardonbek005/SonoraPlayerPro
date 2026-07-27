package com.sonora.player.ui.library

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.sonora.player.R
import com.sonora.player.domain.model.Song
import com.sonora.player.ui.common.AudioPermissionGate
import com.sonora.player.ui.common.SongRow

@Composable
fun LibraryScreen(
    onSongClick: (Song) -> Unit,
    onFilterClick: () -> Unit,
    viewModel: LibraryViewModel = hiltViewModel()
) {
    AudioPermissionGate(onPermissionGranted = { viewModel.refresh() }) {
        val uiState by viewModel.uiState.collectAsState()

        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, top = 16.dp, end = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    GlassNeonSearchField(
                        text = uiState.searchQuery,
                        onTextChanged = viewModel::onSearchQueryChange,
                        onClearClicked = { viewModel.onSearchQueryChange("") }
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                FilterButton(onClick = onFilterClick)
            }
            Spacer(modifier = Modifier.height(8.dp))

            if (uiState.songs.isEmpty() && !uiState.isLoading) {
                EmptyLibraryMessage()
            } else {
                LazyColumn {
                    items(uiState.songs, key = { it.id }) { song ->
                        SongRow(
                            song = song,
                            onClick = { onSongClick(song); viewModel.playSong(song) },
                            onFavoriteClick = { viewModel.toggleFavorite(song.id) }
                        )
                    }
                }
            }
        }
    }
}

/** Small circular button beside the search bar that opens the Equalizer screen. */
@Composable
private fun FilterButton(onClick: () -> Unit) {
    val neonViolet = Color(0xFFA855F7)
    Box(
        modifier = Modifier
            .size(56.dp)
            .clip(CircleShape)
            .background(Color(0x1AFFFFFF))
            .border(width = 1.5.dp, color = neonViolet.copy(alpha = 0.6f), shape = CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(Icons.Filled.Tune, contentDescription = null, tint = neonViolet)
    }
}

/**
 * Signature "glass + neon" search bar: translucent frosted-glass fill, a
 * vertical-gradient border that reads as light hitting glass from above
 * (white highlight fading to a neon-green rim), and an ambient green glow
 * cast outward from the pill shape. Adapted from a design the user supplied,
 * wired to real state and the app's string resources.
 *
 * Note: this uses a fixed neon-green accent rather than the Material You
 * dynamic color, since it's a deliberate signature look rather than a
 * theme-driven default.
 */
@Composable
private fun GlassNeonSearchField(
    text: String,
    onTextChanged: (String) -> Unit,
    onClearClicked: () -> Unit
) {
    val neonGreen = Color(0xFF4ADE80)
    val glassBackground = Color(0x1AFFFFFF)

    val highlightBorder = Brush.verticalGradient(
        colors = listOf(
            Color.White.copy(alpha = 0.7f),
            Color.Transparent,
            neonGreen.copy(alpha = 0.4f)
        )
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        neonGreen.copy(alpha = 0.15f),
                        Color.Transparent,
                        neonGreen.copy(alpha = 0.1f)
                    )
                ),
                shape = RoundedCornerShape(36.dp)
            )
            .border(width = 2.dp, brush = highlightBorder, shape = RoundedCornerShape(36.dp))
            .clip(RoundedCornerShape(36.dp))
            .background(glassBackground)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.Search,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(28.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Box(
                modifier = Modifier
                    .height(30.dp)
                    .width(1.dp)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.White.copy(alpha = 0.4f), Color.Transparent)
                        )
                    )
            )

            Spacer(modifier = Modifier.width(16.dp))

            Box(modifier = Modifier.weight(1f)) {
                if (text.isEmpty()) {
                    Text(
                        text = stringResourceCompat(R.string.search_hint),
                        color = Color.White.copy(alpha = 0.45f),
                        fontSize = 18.sp,
                        maxLines = 1
                    )
                }
                BasicTextField(
                    value = text,
                    onValueChange = onTextChanged,
                    textStyle = TextStyle(
                        color = Color(0xFFF0FDF4),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    cursorBrush = SolidColor(neonGreen),
                    singleLine = true
                )
            }

            if (text.isNotEmpty()) {
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF1E293B).copy(alpha = 0.9f))
                        .border(width = 1.dp, color = neonGreen.copy(alpha = 0.5f), shape = CircleShape)
                        .clickable(onClick = onClearClicked),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = null,
                        tint = neonGreen,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyLibraryMessage() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Filled.MusicNote,
            contentDescription = null,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        Text(stringResourceCompat(R.string.empty_library_title), style = MaterialTheme.typography.titleMedium)
        Text(stringResourceCompat(R.string.empty_library_body), style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun stringResourceCompat(resId: Int): String =
    androidx.compose.ui.res.stringResource(resId)
