package com.noise.meter

import androidx.compose.runtime.Composable
import com.noise.meter.di.AppContainer
import com.noise.meter.ui.noise.NoiseRoute
import com.noise.meter.ui.theme.NoiseMeterTheme

/** The whole app, shared by every platform's entry point. */
@Composable
fun App(container: AppContainer) {
    NoiseMeterTheme {
        NoiseRoute(container)
    }
}
