package com.noise.meter.domain.usecase

import com.noise.meter.domain.model.NoiseLevel
import com.noise.meter.domain.repository.NoiseRecordRepository
import kotlinx.coroutines.flow.first

/** Makes a reading the new record if it is louder than the one held so far. */
class UpdateNoiseRecordUseCase(private val noiseRecordRepository: NoiseRecordRepository) {
    suspend operator fun invoke(level: NoiseLevel) {
        val record = noiseRecordRepository.observe().first()
        if (record == null || level.decibels > record.decibels) noiseRecordRepository.save(level)
    }
}
