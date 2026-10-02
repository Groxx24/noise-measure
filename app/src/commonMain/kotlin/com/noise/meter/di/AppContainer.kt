package com.noise.meter.di

import com.noise.meter.data.audio.microphone
import com.noise.meter.data.record.RecordStore
import com.noise.meter.data.repository.MicrophoneNoiseRepository
import com.noise.meter.data.repository.StoredNoiseRecordRepository
import com.noise.meter.domain.repository.NoiseRecordRepository
import com.noise.meter.domain.repository.NoiseRepository
import com.noise.meter.domain.usecase.ObserveNoiseLevelUseCase
import com.noise.meter.domain.usecase.ObserveNoiseRecordUseCase
import com.noise.meter.domain.usecase.UpdateNoiseRecordUseCase

/**
 * The one place where dependencies are wired together, by hand until a DI library is chosen.
 * Each platform creates a single instance and hands in what only it can build.
 */
class AppContainer(recordStore: RecordStore) {
    private val noiseRepository: NoiseRepository by lazy { MicrophoneNoiseRepository(microphone()) }
    private val noiseRecordRepository: NoiseRecordRepository by lazy {
        StoredNoiseRecordRepository(recordStore)
    }

    val observeNoiseLevel by lazy { ObserveNoiseLevelUseCase(noiseRepository) }
    val observeNoiseRecord by lazy { ObserveNoiseRecordUseCase(noiseRecordRepository) }
    val updateNoiseRecord by lazy { UpdateNoiseRecordUseCase(noiseRecordRepository) }
}
