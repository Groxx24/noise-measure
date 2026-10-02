package com.noise.meter.domain.repository

import com.noise.meter.domain.model.NoiseLevel
import kotlinx.coroutines.flow.Flow

interface NoiseRecordRepository {
    /** Emits the loudest reading ever saved, or null until there is one. It outlives the app. */
    fun observe(): Flow<NoiseLevel?>

    suspend fun save(record: NoiseLevel)
}
