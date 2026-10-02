package com.noise.meter.ui.noise

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.noise.meter.domain.model.NoiseBand
import com.noise.meter.domain.usecase.ObserveNoiseLevelUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlin.math.roundToInt

data class NoiseUiState(
    val decibels: Int = 0,
    val band: NoiseBand = NoiseBand.Quiet,
    /** How far up the scale the reading is, from 0 to 1. */
    val level: Float = 0f,
    val isMicrophoneAvailable: Boolean = true,
)

class NoiseViewModel(observeNoiseLevel: ObserveNoiseLevelUseCase) : ViewModel() {

    /**
     * The microphone is on only while this is collected: it starts with the first collector,
     * stops as soon as the last one leaves (the app going to the background included) and is
     * tried again on return. Collect it only once recording has been allowed.
     */
    val uiState: StateFlow<NoiseUiState> =
        observeNoiseLevel()
            .map { noise ->
                NoiseUiState(
                    decibels = noise.decibels.roundToInt(),
                    band = noise.band,
                    level = noise.fraction,
                )
            }
            .catch { emit(NoiseUiState(isMicrophoneAvailable = false)) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), NoiseUiState())
}
