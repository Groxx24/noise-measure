package com.noise.meter.domain.usecase

import com.noise.meter.domain.model.NoiseLevel
import com.noise.meter.domain.repository.NoiseRecordRepository
import kotlinx.coroutines.flow.Flow

class ObserveNoiseRecordUseCase(private val noiseRecordRepository: NoiseRecordRepository) {
    operator fun invoke(): Flow<NoiseLevel?> = noiseRecordRepository.observe()
}
