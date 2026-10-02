package com.noise.meter.domain.usecase

import com.noise.meter.domain.model.NoiseLevel
import com.noise.meter.domain.repository.NoiseRepository
import kotlinx.coroutines.flow.Flow

class ObserveNoiseLevelUseCase(private val noiseRepository: NoiseRepository) {
    operator fun invoke(): Flow<NoiseLevel> = noiseRepository.observe()
}
