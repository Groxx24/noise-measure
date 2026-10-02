package com.noise.meter.ui.theme

import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp

val Backdrop = Color(0xFF071211)
val Calm = Color(0xFF14B8A6)

/** Where on the scale (0 to 1) each colour is reached; levels in between blend the two around them. */
private val NoiseColorStops = listOf(
    0.30f to Calm,
    0.45f to Color(0xFF22C55E),
    0.58f to Color(0xFFF59E0B),
    0.71f to Color(0xFFEF4444),
)

/** The colour for a noise level between 0 (silent) and 1 (deafening): teal, green, amber, red. */
fun noiseColor(level: Float): Color {
    val next = NoiseColorStops.indexOfFirst { (stop, _) -> level < stop }
    if (next == 0) return NoiseColorStops.first().second
    if (next == -1) return NoiseColorStops.last().second
    val (fromStop, fromColor) = NoiseColorStops[next - 1]
    val (toStop, toColor) = NoiseColorStops[next]
    return lerp(fromColor, toColor, (level - fromStop) / (toStop - fromStop))
}

private val NoiseColorScheme = darkColorScheme(
    primary = Calm,
    onPrimary = Backdrop,
    background = Backdrop,
    onBackground = Color.White,
    surface = Backdrop,
    onSurface = Color.White,
)

@Composable
fun NoiseMeterTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = NoiseColorScheme) {
        // Everything sits straight on the coloured backdrop, never on a Material surface.
        CompositionLocalProvider(
            LocalContentColor provides MaterialTheme.colorScheme.onBackground,
            content = content,
        )
    }
}
