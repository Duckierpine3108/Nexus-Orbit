package com.nexus.orbit.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = OrbitCyanAccent,
    background = OrbitDeepBlack,
    surface = OrbitSurfaceDark,
    onPrimary = OrbitDeepBlack,
    onBackground = OrbitTextPrimary,
    onSurface = OrbitTextPrimary
)

@Composable
fun NexusOrbitTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        content = content
    )
}