package com.sonora.player.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

// Original fallback palette for devices below Android 12 (no Material You).
private val SonoraDarkFallback = darkColorScheme(
    primary = Color(0xFF7BE0AD),
    secondary = Color(0xFFF2A65A),
    tertiary = Color(0xFFE85D75),
    background = Color(0xFF121016),
    surface = Color(0xFF1B1922)
)

private val SonoraLightFallback = lightColorScheme(
    primary = Color(0xFF0F6E6B),
    secondary = Color(0xFFC97A2E),
    tertiary = Color(0xFFB0304A),
    background = Color(0xFFFBF9F5),
    surface = Color(0xFFF2EFE8)
)

@Composable
fun SonoraTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S ->
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        darkTheme -> SonoraDarkFallback
        else -> SonoraLightFallback
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = SonoraTypography,
        content = content
    )
}
