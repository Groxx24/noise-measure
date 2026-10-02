package com.noise.meter.ui.noise

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.noise.meter.di.AppContainer
import com.noise.meter.domain.model.NoiseBand
import com.noise.meter.resources.Res
import com.noise.meter.resources.band_harmful
import com.noise.meter.resources.band_loud
import com.noise.meter.resources.band_moderate
import com.noise.meter.resources.band_quiet
import com.noise.meter.resources.measuring_start
import com.noise.meter.resources.measuring_stop
import com.noise.meter.resources.microphone_unavailable
import com.noise.meter.resources.permission_allow
import com.noise.meter.resources.permission_body
import com.noise.meter.resources.permission_title
import com.noise.meter.resources.record_label
import com.noise.meter.resources.record_value
import com.noise.meter.resources.unit_db
import com.noise.meter.ui.permission.rememberMicrophonePermission
import com.noise.meter.ui.theme.Backdrop
import com.noise.meter.ui.theme.NoiseMeterTheme
import com.noise.meter.ui.theme.noiseColor
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun NoiseRoute(container: AppContainer, modifier: Modifier = Modifier) {
    val permission = rememberMicrophonePermission()
    if (permission.isGranted) {
        // The state is collected only here, so the microphone stays off until it is allowed.
        val viewModel = viewModel { NoiseViewModel(container.observeNoiseLevel) }
        val state by viewModel.uiState.collectAsStateWithLifecycle()
        NoiseScreen(
            state = state,
            onMeasuringChange = viewModel::setMeasuring,
            modifier = modifier,
        )
    } else {
        // Ask once without waiting for a tap; the button is for when that was turned down.
        var hasAsked by rememberSaveable { mutableStateOf(false) }
        LaunchedEffect(Unit) {
            if (!hasAsked) {
                hasAsked = true
                permission.request()
            }
        }
        PermissionScreen(onAllow = permission.request, modifier = modifier)
    }
}

/** The whole screen is the meter: a bar that fills it from the bottom, coloured by how loud it is. */
@Composable
fun NoiseScreen(
    state: NoiseUiState,
    onMeasuringChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    // A soft spring lets the bar overshoot and settle, so every jump in noise lands with a bump.
    val level = animateFloatAsState(
        targetValue = state.level,
        animationSpec = spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessLow),
        label = "level",
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            // Read in the draw phase, so the animation repaints without recomposing the screen.
            .drawBehind { drawMeter(level.value) }
            .safeDrawingPadding()
            .padding(24.dp),
    ) {
        state.recordDecibels?.let { Record(decibels = it, modifier = Modifier.align(Alignment.TopEnd)) }
        if (state.isMeasuring) {
            Box(modifier = Modifier.align(Alignment.Center)) {
                Readout(decibels = state.decibels, band = state.band)
            }
            StopButton(
                onClick = { onMeasuringChange(false) },
                modifier = Modifier.align(Alignment.BottomCenter),
            )
        } else {
            // Equal weights above and below keep the button in the exact middle of the screen.
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Box(
                    modifier = Modifier.weight(1f).padding(bottom = 16.dp),
                    contentAlignment = Alignment.BottomCenter,
                ) {
                    if (!state.isMicrophoneAvailable) {
                        Message(stringResource(Res.string.microphone_unavailable))
                    }
                }
                StartButton(onClick = { onMeasuringChange(true) })
                Spacer(modifier = Modifier.weight(1f))
            }
        }
    }
}

private fun DrawScope.drawMeter(level: Float) {
    val color = noiseColor(level)
    drawRect(lerp(color, Backdrop, 0.78f))

    val top = size.height * (1f - level)
    val corner = 36.dp.toPx()
    drawRoundRect(
        brush = Brush.verticalGradient(
            colors = listOf(color, lerp(color, Color.Black, 0.35f)),
            startY = top,
            endY = size.height,
        ),
        topLeft = Offset(0f, top),
        // Taller than what is visible, which keeps the bottom corners off screen and square.
        size = Size(size.width, size.height - top + corner),
        cornerRadius = CornerRadius(corner),
    )
}

@Composable
private fun Readout(decibels: Int, band: NoiseBand) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = decibels.toString(),
                modifier = Modifier.alignByBaseline(),
                style = ReadoutStyle,
            )
            Text(
                text = stringResource(Res.string.unit_db),
                modifier = Modifier.alignByBaseline(),
                style = ReadoutStyle.copy(fontSize = 28.sp, fontWeight = FontWeight.Medium),
            )
        }
        Text(
            text = stringResource(band.label),
            style = ReadoutStyle.copy(fontSize = 22.sp, fontWeight = FontWeight.Medium),
        )
    }
}

/** The loudest reading so far. */
@Composable
private fun Record(decibels: Int, modifier: Modifier = Modifier) {
    Column(modifier = modifier, horizontalAlignment = Alignment.End) {
        Text(
            text = stringResource(Res.string.record_label),
            style = ReadoutStyle.copy(fontSize = 14.sp, fontWeight = FontWeight.Medium),
        )
        Text(
            text = stringResource(Res.string.record_value, decibels),
            style = ReadoutStyle.copy(fontSize = 28.sp),
        )
    }
}

/** Big and round in the middle of the screen: while stopped it is all there is to do. */
@Composable
private fun StartButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Button(
        onClick = onClick,
        modifier = modifier.size(176.dp),
        shape = CircleShape,
        colors = MeterButtonColors,
    ) {
        Text(
            text = stringResource(Res.string.measuring_start),
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
        )
    }
}

@Composable
private fun StopButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Button(
        onClick = onClick,
        modifier = modifier.defaultMinSize(minWidth = 160.dp, minHeight = 56.dp),
        colors = MeterButtonColors,
    ) {
        Text(
            text = stringResource(Res.string.measuring_stop),
            style = MaterialTheme.typography.titleMedium,
        )
    }
}

/** White stays readable over every colour the bar takes. */
private val MeterButtonColors: ButtonColors
    @Composable get() = ButtonDefaults.buttonColors(
        containerColor = MaterialTheme.colorScheme.onBackground,
        contentColor = MaterialTheme.colorScheme.background,
    )

private val NoiseBand.label: StringResource
    get() = when (this) {
        NoiseBand.Quiet -> Res.string.band_quiet
        NoiseBand.Moderate -> Res.string.band_moderate
        NoiseBand.Loud -> Res.string.band_loud
        NoiseBand.Harmful -> Res.string.band_harmful
    }

private val ReadoutStyle = TextStyle(
    fontSize = 128.sp,
    fontWeight = FontWeight.Black,
    // Same-width digits, so the number doesn't shuffle sideways as it changes.
    fontFeatureSettings = "tnum",
    // Keeps white text readable when the amber part of the bar is behind it.
    shadow = Shadow(color = Color.Black.copy(alpha = 0.3f), blurRadius = 24f),
)

@Composable
fun PermissionScreen(onAllow: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .safeDrawingPadding()
            .padding(32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = stringResource(Res.string.permission_title),
            style = MaterialTheme.typography.headlineSmall,
        )
        Message(stringResource(Res.string.permission_body))
        Button(onClick = onAllow) {
            Text(stringResource(Res.string.permission_allow))
        }
    }
}

@Composable
private fun Message(text: String) {
    Text(
        text = text,
        modifier = Modifier.padding(horizontal = 32.dp),
        textAlign = TextAlign.Center,
        style = MaterialTheme.typography.bodyLarge,
    )
}

@Preview
@Composable
private fun NoiseScreenPreview() {
    NoiseMeterTheme {
        NoiseScreen(
            state = NoiseUiState(
                isMeasuring = true,
                decibels = 72,
                band = NoiseBand.Loud,
                level = 0.6f,
                recordDecibels = 94,
            ),
            onMeasuringChange = {},
        )
    }
}
