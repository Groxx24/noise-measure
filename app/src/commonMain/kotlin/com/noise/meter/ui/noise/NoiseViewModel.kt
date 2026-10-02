package com.noise.meter.ui.noise

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.noise.meter.domain.model.NoiseBand
import com.noise.meter.domain.usecase.ObserveNoiseLevelUseCase
import com.noise.meter.domain.usecase.ObserveNoiseRecordUseCase
import com.noise.meter.domain.usecase.UpdateNoiseRecordUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlin.math.roundToInt

data class NoiseUiState(
    val isMeasuring: Boolean = false,
    val decibels: Int = 0,
    val band: NoiseBand = NoiseBand.Quiet,
    /** How far up the scale the reading is, from 0 to 1. */
    val level: Float = 0f,
    /** The loudest reading ever, or null before the first one. */
    val recordDecibels: Int? = null,
    val isMicrophoneAvailable: Boolean = true,
)

class NoiseViewModel(
    private val observeNoiseLevel: ObserveNoiseLevelUseCase,
    observeNoiseRecord: ObserveNoiseRecordUseCase,
    private val updateNoiseRecord: UpdateNoiseRecordUseCase,
) : ViewModel() {

    private val isMeasuring = MutableStateFlow(false)
    private val isMicrophoneAvailable = MutableStateFlow(true)

    /**
     * The microphone is on only while measuring has been started and this is collected: it stops
     * as soon as the last collector leaves (the app going to the background included) and is
     * tried again on return. Collect it only once recording has been allowed.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<NoiseUiState> =
        combine(
            isMeasuring.flatMapLatest { measuring -> if (measuring) readings() else flowOf(NoiseUiState()) },
            observeNoiseRecord(),
            isMicrophoneAvailable,
        ) { reading, record, available ->
            reading.copy(
                recordDecibels = record?.decibels?.roundToInt(),
                isMicrophoneAvailable = available,
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), NoiseUiState())

    fun setMeasuring(measuring: Boolean) {
        if (measuring) isMicrophoneAvailable.value = true
        isMeasuring.value = measuring
    }

    private fun readings(): Flow<NoiseUiState> =
        observeNoiseLevel()
            .onEach { noise -> updateNoiseRecord(noise) }
            .map { noise ->
                NoiseUiState(
                    isMeasuring = true,
                    decibels = noise.decibels.roundToInt(),
                    band = noise.band,
                    level = noise.fraction,
                )
            }
            // The microphone takes a moment to open; show that measuring has begun straight away.
            .onStart { emit(NoiseUiState(isMeasuring = true)) }
            .catch {
                isMicrophoneAvailable.value = false
                isMeasuring.value = false
            }
}
