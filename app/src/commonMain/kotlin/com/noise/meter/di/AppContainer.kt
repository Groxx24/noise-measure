package com.noise.meter.di

import com.noise.meter.data.audio.microphone
import com.noise.meter.data.repository.MicrophoneNoiseRepository
import com.noise.meter.domain.repository.NoiseRepository
import com.noise.meter.domain.usecase.ObserveNoiseLevelUseCase

/**
 * The one place where dependencies are wired together, by hand until a DI library is chosen.
 * Each platform creates a single instance.
 */
class AppContainer {
    private val noiseRepository: NoiseRepository by lazy { MicrophoneNoiseRepository(microphone()) }

    val observeNoiseLevel by lazy { ObserveNoiseLevelUseCase(noiseRepository) }
}
