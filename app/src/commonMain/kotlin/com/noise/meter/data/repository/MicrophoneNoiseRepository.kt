package com.noise.meter.data.repository

import com.noise.meter.data.audio.AudioSource
import com.noise.meter.domain.model.NoiseLevel
import com.noise.meter.domain.repository.NoiseRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class MicrophoneNoiseRepository(private val audioSource: AudioSource) : NoiseRepository {
    override fun observe(): Flow<NoiseLevel> =
        audioSource.levels().map { dbfs ->
            NoiseLevel((dbfs + FullScaleDecibels).coerceIn(NoiseLevel.Range))
        }

    private companion object {
        /**
         * The sound pressure taken to drive a phone microphone to full scale. Android asks
         * manufacturers for 90 dB SPL to read about -22 dBFS; no phone is calibrated beyond that,
         * so readings are a good estimate, not a measurement.
         */
        const val FullScaleDecibels = 112f
    }
}
