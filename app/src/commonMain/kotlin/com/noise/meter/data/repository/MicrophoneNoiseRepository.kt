package com.noise.meter.data.repository

import com.noise.meter.data.audio.AudioSource
import com.noise.meter.domain.model.NoiseLevel
import com.noise.meter.domain.repository.NoiseRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class MicrophoneNoiseRepository(private val audioSource: AudioSource) : NoiseRepository {
    override fun observe(): Flow<NoiseLevel> =
        audioSource.levels().map { dbfs ->
            // No phone is calibrated, so this is a good estimate, not a measurement.
            NoiseLevel((dbfs + audioSource.fullScaleDecibels).coerceIn(NoiseLevel.Range))
        }
}
